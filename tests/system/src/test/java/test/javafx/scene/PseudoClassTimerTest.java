/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package test.javafx.scene;

import java.util.ArrayList;
import java.util.Collections;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import test.util.Util;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.assertTrue;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;
import javafx.css.PseudoClass;
import static org.junit.Assert.assertEquals;

/**
 * TODO TODO TODO: comment + link this file to a JDK issue number.
 * 
 * 
 */
public class PseudoClassTimerTest {

    private static CountDownLatch startupLatch;
    private static Stage stage;
    private static BorderPane rootPane;

    public static class TestApp extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            stage = primaryStage;
            rootPane = new BorderPane();
            stage.setScene(new Scene(rootPane));
            stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
                Platform.runLater(() -> startupLatch.countDown());
            });
            stage.show();
        }
    }

    @BeforeClass
    public static void initFX() throws Exception {
        startupLatch = new CountDownLatch(1);
        new Thread(() -> Application.launch(TestApp.class, (String[]) null)).start();

        assertTrue("Timeout waiting for FX runtime to start", startupLatch.await(15, TimeUnit.SECONDS));
    }
    static PseudoClass ROOT_PSEUDO_CLASS = PseudoClass.getPseudoClass("root");
    static final ArrayList<PseudoClass> RANDOM_PSEUDO_CLASSES = new ArrayList<>();

    static {
        for (int i = 0; i < 100; i++) {
            RANDOM_PSEUDO_CLASSES.add(PseudoClass.getPseudoClass(("random" + i)));
        }
        Collections.reverse(RANDOM_PSEUDO_CLASSES);
    }

    @Test
    public void testPseudoClassToggleTime() throws Exception {
        System.out.println("Is app thread: " + Platform.isFxApplicationThread());
        ArrayList<HBox> allNodes = new ArrayList<>();
        Util.runAndWait(() -> {
            // Compute time for adding 500 Nodes
            long startTime = System.currentTimeMillis();

            HBox hbox = new HBox();
            for (int i = 0; i < 500; i++) {
                hbox = new HBox(new Text("y"), hbox);
                final HBox h = hbox;
                h.getStyleClass().add("changing");
                allNodes.add(h);
                for (PseudoClass s : RANDOM_PSEUDO_CLASSES) {
                    h.pseudoClassStateChanged(s, true);
                }
                h.setPadding(new Insets(1));
            }
            rootPane.setCenter(hbox);

            long endTime = System.currentTimeMillis();

            System.out.println("Time to create and add 500 nodes to a Scene = "
                    + (endTime - startTime) + " mSec");

            // NOTE : 800 mSec is not a benchmark value
            // It is good enough to catch the regression in performance, if any
            assertTrue("Time to add 500 Nodes is more than 800 mSec", (endTime - startTime) < 800);
            rootPane.getScene().getStylesheets().add(getClass().getResource("pseudo.css").toExternalForm());
        });
        LongProperty startTime = new SimpleLongProperty(System.currentTimeMillis());
        LongProperty totalTime = new SimpleLongProperty(0);
        LongProperty layouts = new SimpleLongProperty(0);
        BooleanProperty toggle = new SimpleBooleanProperty(false);
        final Runtime runtime = Runtime.getRuntime();
        System.out.println("START Free: "+runtime.freeMemory()/1024+" Max: "+runtime.maxMemory()/1024+" Total: "+runtime.totalMemory()/1024);
        CountDownLatch latch = new CountDownLatch(1);
        LongProperty numGC = new SimpleLongProperty(0);
        LongProperty prevFree = new SimpleLongProperty(runtime.freeMemory());
        
        // Measure the time taken to perform 100 layouts while toggling the
        // pseudoclass state of the root node.
        final long maxLayouts = 100;
        Platform.runLater(() -> {
            rootPane.getScene().addPreLayoutPulseListener(() -> {
                startTime.setValue(System.currentTimeMillis());
            });
            rootPane.getScene().addPostLayoutPulseListener(() -> {
                if(layouts.get() < maxLayouts) {
                    long layoutTime = System.currentTimeMillis() - startTime.getValue();
                    totalTime.setValue(totalTime.getValue() + layoutTime);
                    layouts.set(layouts.getValue() + 1);

                    long free = runtime.freeMemory();
                    //System.out.println("Free: "+free+" Max: "+max+" Total: "+total);
                    if(free > prevFree.get()) numGC.set(numGC.get()+1);
                    prevFree.set(free);

                    // Force an update to pseudo-class states down the chain of nodes
                    rootPane.pseudoClassStateChanged(ROOT_PSEUDO_CLASS, !toggle.getValue());
                    toggle.setValue(!toggle.getValue());
//                    for (HBox h : allNodes) {
//                        h.pseudoClassStateChanged(ROOT_PSEUDO_CLASS, toggle.getValue());
//                    }
                
                    Platform.requestNextPulse();
                } else {
                    latch.countDown();
                }
            });
        });
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("End Free: "+runtime.freeMemory()/1024+" Max: "+runtime.maxMemory()/1024+" Total: "+runtime.totalMemory()/1024);
        System.gc();
        System.gc();
        System.out.println("End Free: "+runtime.freeMemory()/1024+" Max: "+runtime.maxMemory()/1024+" Total: "+runtime.totalMemory()/1024);
        System.out.println("Approximate no. garbage collections: " + numGC.get());
        System.out.println(String.format("Total: %dms\tAverage: %.2fms", totalTime.getValue(), totalTime.getValue() / (double) (layouts.getValue())));
        //System.out.println("Created: " + com.sun.javafx.css.PseudoClassState.NUM_CREATED.get());      
        
        assertEquals("Did not finish 100 layouts in under 10 seconds", layouts.get(), maxLayouts);
        // Should be around 300ms to 1660ms depending on hardware after changes.
        // Previously around 2700ms to 4000ms.
        assertTrue("Toggling PseudoClasses takes more than 2500ms", totalTime.getValue() < 2500.0f); 
    }

    @AfterClass
    public static void teardownOnce() {
        Platform.runLater(() -> {
            stage.hide();
            Platform.exit();
        });
    }
}
