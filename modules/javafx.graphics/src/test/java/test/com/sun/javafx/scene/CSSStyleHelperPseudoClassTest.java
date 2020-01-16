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

package test.com.sun.javafx.scene;

import com.sun.javafx.css.StyleManager;
import javafx.stage.Stage;
import com.sun.javafx.tk.Toolkit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.css.CssParser;
import javafx.css.PseudoClass;
import javafx.css.Stylesheet;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CSSStyleHelperPseudoClassTest {

    private Scene scene;
    private Stage stage;
    private Group root;

    @Before public void setup() {
        root = new Group();
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);

        StyleManager sm = StyleManager.getInstance();
        sm.userAgentStylesheetContainers.clear();
        sm.platformUserAgentStylesheetContainers.clear();
        sm.stylesheetContainerMap.clear();
        sm.cacheContainerMap.clear();
        sm.hasDefaultUserAgentStylesheet = false;
    }

    @Test
    public void testCSSPseudoClassesAcrossSeparateNodes() {
        // Test three different pseudo-classes across separate nodes and ensure 
        // the styles update (that applyCSS is called) when the pseudo-class 
        // states are changed.
        Rectangle rect1 = new Rectangle(50,50);
        Rectangle rect2 = new Rectangle(50,50);
        Rectangle rect3 = new Rectangle(50,50);
        PseudoClass ps1 = PseudoClass.getPseudoClass("pseudotest1");
        PseudoClass ps2 = PseudoClass.getPseudoClass("pseudotest2");
        PseudoClass ps3 = PseudoClass.getPseudoClass("pseudotest3");
        Paint defaultFill = rect1.getFill();
        Stylesheet stylesheet = null;
        try {
            stylesheet = new CssParser().parse(
                "testCSSPseudoClassesAcrossSeparateNodes",
                ".rect:pseudotest1 { -fx-fill: red; }\n" +
                ".rect:pseudotest2 { -fx-fill: blue; }\n" +
                ".rect:pseudotest3 { -fx-fill: green; }"
            );
        } catch(IOException ioe) {
            fail();
        }
        rect1.getStyleClass().add("rect");
        rect2.getStyleClass().add("rect");
        rect3.getStyleClass().add("rect");
        root.getChildren().addAll(rect1, rect2, rect3);
        stage.show();

        StyleManager.getInstance().setDefaultUserAgentStylesheet(stylesheet);

        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(defaultFill, rect3.getFill());

        // Changing the root's pseudo-class state shouldn't change the children
        root.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest1"), true);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(defaultFill, rect3.getFill());
        assertEquals(1,root.getPseudoClassStates().size());
        assertTrue(root.getPseudoClassStates().contains(ps1));
        assertTrue(rect1.getPseudoClassStates().isEmpty());
        assertTrue(rect2.getPseudoClassStates().isEmpty());
        assertTrue(rect3.getPseudoClassStates().isEmpty());

        // All three child nodes should be different colours and not affect the
        // root pseudo-class set.
        rect1.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest1"), true);
        rect2.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest2"), true);
        rect3.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest3"), true);
        Toolkit.getToolkit().firePulse();
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(Color.BLUE, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
        assertEquals(1, root.getPseudoClassStates().size());
        assertTrue(root.getPseudoClassStates().contains(ps1));
        assertEquals(1, rect1.getPseudoClassStates().size());
        assertTrue(rect1.getPseudoClassStates().contains(ps1));
        assertEquals(1, rect2.getPseudoClassStates().size());
        assertTrue(rect2.getPseudoClassStates().contains(ps2));
        assertEquals(1, rect3.getPseudoClassStates().size());
        assertTrue(rect3.getPseudoClassStates().contains(ps3));

        // Resetting the root's pseudo-class state shouldn't change the children.
        root.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest1"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(Color.BLUE, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
        assertTrue(root.getPseudoClassStates().isEmpty());
        assertEquals(1, rect1.getPseudoClassStates().size());
        assertTrue(rect1.getPseudoClassStates().contains(ps1));
        assertEquals(1, rect2.getPseudoClassStates().size());
        assertTrue(rect2.getPseudoClassStates().contains(ps2));
        assertEquals(1, rect3.getPseudoClassStates().size());
        assertTrue(rect3.getPseudoClassStates().contains(ps3));

        // Reset the rest one at a time...
        rect1.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest1"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(Color.BLUE, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
        assertTrue(root.getPseudoClassStates().isEmpty());
        assertTrue(rect1.getPseudoClassStates().isEmpty());
        assertEquals(1, rect2.getPseudoClassStates().size());
        assertTrue(rect2.getPseudoClassStates().contains(ps2));
        assertEquals(1, rect3.getPseudoClassStates().size());
        assertTrue(rect3.getPseudoClassStates().contains(ps3));

        rect2.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest2"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
        assertTrue(root.getPseudoClassStates().isEmpty());
        assertTrue(rect1.getPseudoClassStates().isEmpty());
        assertTrue(rect2.getPseudoClassStates().isEmpty());
        assertEquals(1, rect3.getPseudoClassStates().size());
        assertTrue(rect3.getPseudoClassStates().contains(ps3));

        rect3.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest3"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(defaultFill, rect3.getFill());
        assertTrue(root.getPseudoClassStates().isEmpty());
        assertTrue(rect1.getPseudoClassStates().isEmpty());
        assertTrue(rect2.getPseudoClassStates().isEmpty());
        assertTrue(rect3.getPseudoClassStates().isEmpty());
    }

    @Test
    public void testCSSPseudoClassChildSelectorAcrossBranches() {
        // Test one pseudo-class over three branches with different styles for 
        // child nodes. Ensure the pseudo-class states update correctly and 
        // that the style classes update (so the triggerstates update and 
        // applyCSS is called).
        Rectangle rect0 = new Rectangle(50,50);
        Rectangle rect1 = new Rectangle(50,50);
        Rectangle rect2 = new Rectangle(50,50);
        Rectangle rect3 = new Rectangle(50,50);
        Paint defaultFill = rect1.getFill();
        Stylesheet stylesheet = null;
        try {
            stylesheet = new CssParser().parse(
                "testCSSPseudoClassChildSelectorAcrossBranches",
                ".root:hover>.rect { -fx-fill: yellow }\n" +
                ".root1:hover>.rect { -fx-fill: red }\n" +
                ".root2:hover>.rect { -fx-fill: blue }\n" +
                ".root3:hover>.rect { -fx-fill: green }\n" +
                ".rect:hover { -fx-fill: black }" // the child nodes should not have the hover pseudo-class directly
            );
        } catch(IOException ioe) {
            fail();
        }
        rect0.getStyleClass().add("rect");
        rect1.getStyleClass().add("rect");
        rect2.getStyleClass().add("rect");
        rect3.getStyleClass().add("rect");
        Group root1 = new Group(rect1);
        Group root2 = new Group(rect2);
        Group root3 = new Group(rect3);
        root.getStyleClass().add("root");
        root1.getStyleClass().add("root1");
        root2.getStyleClass().add("root2");
        root3.getStyleClass().add("root3");
        root.getChildren().addAll(rect0, root1, root2, root3);
        stage.show();

        StyleManager.getInstance().setDefaultUserAgentStylesheet(stylesheet);

        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect0.getFill());
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(defaultFill, rect3.getFill());

        // Changing the root's pseudo-class state should affect only its
        // immediate child.
        root.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        Toolkit.getToolkit().firePulse();
        assertEquals(Color.YELLOW, rect0.getFill());
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(defaultFill, rect3.getFill());

        // Rest of children should change one at a time.
        root3.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        Toolkit.getToolkit().firePulse();
        assertEquals(Color.YELLOW, rect0.getFill());
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
        root1.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        Toolkit.getToolkit().firePulse();
        assertEquals(Color.YELLOW, rect0.getFill());
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
        root2.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        Toolkit.getToolkit().firePulse();
        assertEquals(Color.YELLOW, rect0.getFill());
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(Color.BLUE, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());

        // Changing the root shouldn't affect grandchild pseudo-classes.
        root.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect0.getFill());
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(Color.BLUE, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());

        // Moving from a branch with an inactive pseudo-class to one where it is
        // active should reflect in the moved child.
        root.getChildren().remove(rect0);
        root2.getChildren().add(rect0);
        Toolkit.getToolkit().firePulse();
        assertEquals(Color.BLUE, rect0.getFill());
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(Color.BLUE, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());

        // Changing a pseudo-class in a branch should reflect in new children.
        root2.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect0.getFill());
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());

        // Old parent's pseudo-class should not update child that was re/moved.
        root.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect0.getFill());
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());

        // Moving to an inactive branch should reflect in the moved child.
        root1.getChildren().remove(rect1);
        root2.getChildren().add(rect1);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect0.getFill());
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
    }

    @Test
    public void testCSSPseudoClassDescendantSelectorPropagatesDownChain() {
        // Test one pseudo-class with a style propagating down a chain of 10
        // nodes.
        List<Pane> panes = new ArrayList<>();
        Pane pane = new Pane();
        pane.getStyleClass().add("pane");
        panes.add(pane);
        pane.setPrefSize(10,10);
        for(int i = 1; i < 10; i++) {
            Pane p = new Pane();
            p.getStyleClass().add("pane");
            panes.add(p);
            p.setPrefSize(10,10);
            pane.getChildren().add(p);
            pane = p;
        }
        Stylesheet stylesheet = null;
        Pane redPane = new Pane();
        redPane.setPrefSize(10,10);
        redPane.getStyleClass().add("redpane");
        Pane bluePane = new Pane();
        redPane.setPrefSize(10,10);
        bluePane.getStyleClass().add("bluepane");
        try {
            stylesheet = new CssParser().parse(
                "testCSSPseudoClassDescendantSelectorPropagatesDownChain",
                ".redroot:hover *.pane { -fx-background-color: red }\n" +
                ".bluesubroot:hover *.pane { -fx-background-color: blue}\n" +
                ".redpane {-fx-background-color: red}" +
                ".bluepane {-fx-background-color: blue}"
            );
        } catch(IOException ioe) {
            fail();
        }
        Group redroot = new Group();
        Group bluesubroot = new Group();
        redroot.getStyleClass().add("redroot");
        bluesubroot.getStyleClass().add("bluesubroot");
        root.getChildren().addAll(redroot, redPane, bluePane);
        // start off with the chain as part of the red root
        redroot.getChildren().add(panes.get(0));
        stage.show();

        StyleManager.getInstance().setDefaultUserAgentStylesheet(stylesheet);

        Toolkit.getToolkit().firePulse();
        for(Pane p : panes)
            assertEquals(null, p.getBackground());
        List<BackgroundFill> redFills = redPane.getBackground().getFills();
        List<BackgroundFill> blueFills = bluePane.getBackground().getFills();

        // Changing the root's pseudo-class state should affect all the children
        // down the chain.
        redroot.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        Toolkit.getToolkit().firePulse();
        for(Pane p : panes)
            assertTrue(redFills.equals(p.getBackground().getFills()));

        // Deactivating the pseudo-class should reset the child node styles.
        redroot.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
        Toolkit.getToolkit().firePulse();
        for(Pane p : panes) {
            assertEquals(null, p.getBackground());
        }

        // Insert the blue sub-root half-way down the chain of nodes
        Pane midPoint = panes.get(4);
        Node endHalfOfChain = panes.get(5);
        midPoint.getChildren().remove(endHalfOfChain);
        bluesubroot.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        bluesubroot.getChildren().add(endHalfOfChain);
        midPoint.getChildren().add(bluesubroot);
        Toolkit.getToolkit().firePulse();
        for(int i = 0; i < 10; i++) {
            Pane p = panes.get(i);
            if(i < 5) assertEquals(null, p.getBackground());
            else {
                assertTrue(blueFills.equals(p.getBackground().getFills()));
            }
        }

        // Deactivating the pseudo-class in the blue sub-root should reset the
        // child styles.
        bluesubroot.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
        Toolkit.getToolkit().firePulse();
        for(Pane p : panes)
            assertEquals(null, p.getBackground());

        // Activating the pseudo-class in the red root should affect all the
        // children in the chain.
        redroot.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        Toolkit.getToolkit().firePulse();
        for(Pane p : panes)
            assertTrue(redFills.equals(p.getBackground().getFills()));
    }

}
