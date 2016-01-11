package com.vitareminder.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.Vector;


/**
 * The tab order that is set to {@code VitaReminderFrame}.  This implementation
 * accounts for potentially disabled buttons and skips over them.
 */
public class PanelFocusTraversalPolicy extends FocusTraversalPolicy
{
    Vector<Component> order;

    public PanelFocusTraversalPolicy(Vector<Component> order)
    {
        this.order = new Vector<Component>(order.size());
        this.order.addAll(order);
    }


    /**
     * Returns the component that should get the focus after {@code component}.
     * If the parameter component is the last component, it wraps to the beginning.
     * Otherwise, it moves to the next component.  If this is enabled, it is returned.
     * If all components are disabled, it will return <tt>null</tt>.
     */
    @Override
    public Component getComponentAfter(Container focusCycleRoot, Component component)
    {
        int index = order.indexOf(component);

        for (int i = 0; i < order.size(); i++)
        {
            if (index == (order.size() - 1))
            {
                index = 0;
            }
            else
            {
                index++;
            }

            Component next = order.get(index);

            if (next.isEnabled())
            {
                return next;
            }
        }

        return null;
    }


    /**
     * Returns the component that should get the focus before {@code component}.
     * If it reaches the first component, it wraps to the last componenent.  If
     * the component is enabled, it returns it.  If no components are enabled,
     * it returns <tt>null</tt>.
     */
    @Override
    public Component getComponentBefore(Container focusCycleRoot, Component component)
    {
        int index = order.indexOf(component);

        for (int i = 0; i < order.size(); i++)
        {
            index -= 1;

            if (index < 0)
            {
                index = order.size() - 1;
            }

            Component previous = order.get(index);

            if (previous.isEnabled())
            {
                return previous;
            }
        }

        return null;
    }


    /**
     * Returns the first component that should receive focus.
     */
    @Override
    public Component getDefaultComponent(Container focusCycleRoot)
    {
        return getFirstComponent(focusCycleRoot);
    }


    /**
     * Returns the last component in the traversal cycle.  This implementation
     * will not return the component if it is currently disabled.  In that case,
     * it will return the next component in the traversal cycle.
     */
    @Override
    public Component getLastComponent(Container focusCycleRoot)
    {
        Component component = order.lastElement();

        if (component.isEnabled())
        {
            return component;
        }
        else
        {
            return getComponentBefore(focusCycleRoot, component);
        }
    }


    /**
     * Returns the first component in the traversal cycle.  This implementation
     * will not return the component if it is currently disabled.  In that case,
     * it will return the next component in the traversal cycle.
     */
    @Override
    public Component getFirstComponent(Container focusCycleRoot)
    {
        Component component = order.get(0);

        if (component.isEnabled())
        {
            return component;
        }
        else
        {
            return getComponentAfter(focusCycleRoot, component);
        }
    }

}  // end class PanelFocusTraversalPolicy
