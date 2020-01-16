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
import javafx.css.CssParser;
import javafx.css.PseudoClass;
import javafx.css.Stylesheet;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import org.junit.Before;
import org.junit.Test;

public class CSSStyleHelperPseudoClassTest {
    
    private Scene scene;
    private Stage stage;
    private Group root;
    
    @Before public void setup() {
        root = new Group();
        Toolkit tk = Toolkit.getToolkit();
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
    public void testSimplePseudoClass() {

        Rectangle rect = new Rectangle(50,50);
        Paint defaultFill = rect.getFill();
        Stylesheet stylesheet = null;
        try {
            // Note: setDefaultUserAgentStylesheet in StyleManager won't replace the UA stylesheet unless it has a name,
            //       and that name needs to be different from the current one, if any. This matters when running
            //       these tests from the same VM since StyleManager is a singleton.
            stylesheet = new CssParser().parse(
                "testSimplePseudoClass",
                ".rect:pseudotest1 { -fx-fill: red; }"
            );
        } catch(IOException ioe) {
            fail();
        }
        rect.getStyleClass().add("rect");
        root.getChildren().add(rect);
        stage.show();
        
        StyleManager.getInstance().setDefaultUserAgentStylesheet(stylesheet);

        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect.getFill());

        rect.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest1"), true);
        Toolkit.getToolkit().firePulse();

        assertEquals(Color.RED, rect.getFill());
        rect.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest1"), false);
        Toolkit.getToolkit().firePulse();

        assertEquals(defaultFill, rect.getFill());
    }
    
    @Test
    public void testSimplePseudoClassBranches() {
        Rectangle rect1 = new Rectangle(50,50);
        Rectangle rect2 = new Rectangle(50,50);
        Rectangle rect3 = new Rectangle(50,50);
        Paint defaultFill = rect1.getFill();
        Stylesheet stylesheet = null;
        try {
            // Note: setDefaultUserAgentStylesheet in StyleManager won't replace the UA stylesheet unless it has a name,
            //       and that name needs to be different from the current one, if any. This matters when running
            //       these tests from the same VM since StyleManager is a singleton.
            stylesheet = new CssParser().parse(
                "testSimplePseudoClassBranches",
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

        // All three should be different colours
        rect1.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest1"), true);
        rect2.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest2"), true);
        rect3.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest3"), true);
        Toolkit.getToolkit().firePulse();
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(Color.BLUE, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());

        // Resetting the root shouldn't change the children
        root.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest1"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(Color.RED, rect1.getFill());
        assertEquals(Color.BLUE, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
        
        // Reset the rest one at a time...
        rect1.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest1"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(Color.BLUE, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
        rect2.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest2"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(Color.GREEN, rect3.getFill());
        rect3.pseudoClassStateChanged(PseudoClass.getPseudoClass("pseudotest3"), false);
        Toolkit.getToolkit().firePulse();
        assertEquals(defaultFill, rect1.getFill());
        assertEquals(defaultFill, rect2.getFill());
        assertEquals(defaultFill, rect3.getFill());
    }
    
    @Test
    public void testChildSelectorPseudoClass() {
        Rectangle rect0 = new Rectangle(50,50);
        Rectangle rect1 = new Rectangle(50,50);
        Rectangle rect2 = new Rectangle(50,50);
        Rectangle rect3 = new Rectangle(50,50);
        Paint defaultFill = rect1.getFill();
        Stylesheet stylesheet = null;
        try {
            // Note: setDefaultUserAgentStylesheet in StyleManager won't replace the UA stylesheet unless it has a name,
            //       and that name needs to be different from the current one, if any. This matters when running
            //       these tests from the same VM since StyleManager is a singleton.
            stylesheet = new CssParser().parse(
                "testChildSelectorPseudoClass",
                ".root:hover *.rect { -fx-fill: yellow }\n" +
                ".root1:hover *.rect { -fx-fill: red }\n" +
                ".root2:hover *.rect { -fx-fill: blue }\n" +
                ".root3:hover *.rect { -fx-fill: green }\n" +
                ".rect:random { -fx-fill: black }" // check in case the helper sets random states
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
        root1.getStyleClass().add("root");
        root2.getStyleClass().add("root");
        root3.getStyleClass().add("root");
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
        root1.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
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
        
        // Old parent pseudo-class should not update child that was re/moved.
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
    
}
