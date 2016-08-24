/* -----------------------------------------------------------------------------
 * Antenna - An Ant-to-end solution for wireless Java 
 *
 * Copyright (c) 2002-2004 Joerg Pleumann <joerg@pleumann.de>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * -----------------------------------------------------------------------------
 */
package de.pleumann.antenna.misc;

import org.apache.tools.ant.Project;

/**
 * A class that is used as the basis for nested elements in the
 * other tasks. it provides "if" and "unless" parameters and thus
 * allows for conditional use of nested elements based on property
 * definitions.
 */
public class Conditional {

    private Project project;
        
    private String ifExpr;
    
    private String unlessExpr;
    
    public Conditional(Project project) {
        this.project = project;
    }
    
    public void setIf(String s) {
        ifExpr = s;
    }
    
    public void setUnless(String s) {
        unlessExpr = s;
    }

    public boolean isActive() {
        if (ifExpr != null && project.getProperty(ifExpr) == null) {
            return false;
        }

        if (unlessExpr != null && project.getProperty(unlessExpr) != null) {
            return false;
        }
        
        return true;
    }
}
