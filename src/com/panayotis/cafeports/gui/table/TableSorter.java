/*
Copyright 1994-2009 Sun Microsystems, Inc. All Rights Reserved.
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:


 * Redistribution of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

 * Redistribution in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.


Neither the name of Sun Microsystems, Inc. or the names of contributors may be used to endorse or promote products derived from this software without specific prior written permission.

This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

You acknowledge that this software is not designed, licensed or intended for use in the design, construction, operation or maintenance of any nuclear facility.
 */
package com.panayotis.cafeports.gui.table;

import com.panayotis.utilities.Closure;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * TableSorter is a decorator for TableModels; adding sorting
 * functionality to a supplied TableModel. TableSorter does
 * not store or copy the data in its TableModel; instead it maintains
 * a map from the row indexes of the view to the row indexes of the
 * model. As requests are made of the sorter (like getValueAt(row, col))
 * they are passed to the underlying model after the row numbers
 * have been translated via the internal mapping array. This way,
 * the TableSorter appears to hold another copy of the table
 * with the rows in a different order.
 * <p/>
 * TableSorter registers itself as a listener to the underlying model,
 * just as the JTable itself would. Events recieved from the model
 * are examined, sometimes manipulated (typically widened), and then
 * passed on to the TableSorter's listeners (typically the JTable).
 * If a change to the model has invalidated the order of TableSorter's
 * rows, a note of this is made and the sorter will resort the
 * rows the next time a value is requested.
 * <p/>
 * When the tableHeader property is set, either by using the
 * setTableHeader() method or the two argument constructor, the
 * table header may be used as a complete UI for TableSorter.
 * The default renderer of the tableHeader is decorated with a renderer
 * that indicates the sorting status of each column. In addition,
 * a mouse listener is installed with the following behavior:
 * <ul>
 * <li>
 * Mouse-click: Clears the sorting status of all other columns
 * and advances the sorting status of that column through three
 * values: {NOT_SORTED, ASCENDING, DESCENDING} (then back to
 * NOT_SORTED again).
 * <li>
 * SHIFT-mouse-click: Clears the sorting status of all other columns
 * and cycles the sorting status of the column through the same
 * three values, in the opposite order: {NOT_SORTED, DESCENDING, ASCENDING}.
 * <li>
 * CONTROL-mouse-click and CONTROL-SHIFT-mouse-click: as above except
 * that the changes to the column do not cancel the statuses of columns
 * that are already sorting - giving a way to initiate a compound
 * sort.
 * </ul>
 * <p/>
 * This class first appeared in the swing table demos in 1997 (v1.5) and then
 * had a major rewrite in 2004 (v2.0) to make it compatible with Java 1.4.
 * <p/>
 * This rewrite makes the class compile cleanly with Java 1.5 while
 * maintaining backward compatibility with TableSorter v2.0.
 *
 * @author Philip Milne
 * @author Brendon McLean
 * @author Dan van Enckevort
 * @author Parwinder Sekhon
 * @author ouroborus@ouroborus.org
 * @version 2.1 04/29/06
 */
public class TableSorter extends AbstractTableModel {

    public JPortList hack_for_table_columns_update;
    /* */
    protected TableModel tableModel;
    public static final int DESCENDING = -1;
    public static final int NOT_SORTED = 0;
    public static final int ASCENDING = 1;
    private static Directive EMPTY_DIRECTIVE = new Directive(-1, NOT_SORTED);
    public static final Comparator<Object> COMPARABLE_COMPARATOR = new Comparator<Object>() {

        public int compare(Object o1, Object o2) {
            Method m;
            try {
                // See if o1 is capable of comparing itself to o2
                m = o1.getClass().getDeclaredMethod("compareTo", o2.getClass());
            } catch (NoSuchMethodException e) {
                throw new ClassCastException();
            }

            Object retVal;
            try {
                // make the comparison
                retVal = m.invoke(o1, o2);
            } catch (IllegalAccessException e) {
                throw new ClassCastException();
            } catch (InvocationTargetException e) {
                throw new ClassCastException();
            }

            // Comparable.compareTo() is supposed to return int but invoke()
            // returns Object. We can't cast an Object to an int but we can
            // cast it to an Integer and then extract the int from the Integer.
            // But first, make sure it can be done.
            Integer i = new Integer(0);
            if (!i.getClass().isInstance(retVal))
                throw new ClassCastException();

            return i.getClass().cast(retVal).intValue();
        }
    };
    public static final Comparator<Object> LEXICAL_COMPARATOR = new Comparator<Object>() {

        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };
    private Row[] viewToModel;
    private int[] modelToView;
    private JTableHeader tableHeader;
    private MouseListener mouseListener;
    private TableModelListener tableModelListener;
    private Map<Class, Comparator<Object>> columnComparators = new HashMap<Class, Comparator<Object>>();
    private List<Directive> sortingColumns = new ArrayList<Directive>();

    public TableSorter() {
        this.mouseListener = new MouseHandler();
        this.tableModelListener = new TableModelHandler();
    }

    public TableSorter(TableModel tableModel) {
        this();
        setTableModel(tableModel);
    }

    public TableSorter(TableModel tableModel, JTableHeader tableHeader) {
        this();
        setTableHeader(tableHeader);
        setTableModel(tableModel);
    }

    private void clearSortingState() {
        viewToModel = null;
        modelToView = null;
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(TableModel tableModel) {
        if (this.tableModel != null)
            this.tableModel.removeTableModelListener(tableModelListener);

        this.tableModel = tableModel;
        if (this.tableModel != null)
            this.tableModel.addTableModelListener(tableModelListener);

        clearSortingState();
        fireTableStructureChanged();
    }

    public JTableHeader getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(JTableHeader tableHeader) {
        if (this.tableHeader != null)
            this.tableHeader.removeMouseListener(mouseListener);
        this.tableHeader = tableHeader;
        if (this.tableHeader != null) {
            this.tableHeader.addMouseListener(mouseListener);
            this.tableHeader.setDefaultRenderer(new SortableHeaderRenderer());
        }
    }

    public boolean isSorting() {
        return sortingColumns.size() != 0;
    }

    private Directive getDirective(int column) {
        for (int i = 0; i < sortingColumns.size(); i++) {
            Directive directive = sortingColumns.get(i);
            if (directive.column == column)
                return directive;
        }
        return EMPTY_DIRECTIVE;
    }

    public int getSortingStatus(int column) {
        return getDirective(column).direction;
    }

    private void sortingStatusChanged() {
        clearSortingState();
        fireTableDataChanged();
        if (tableHeader != null)
            tableHeader.repaint();
    }

    public void setSortingStatus(int column, int status) {
        Directive directive = getDirective(column);
        if (directive != EMPTY_DIRECTIVE)
            sortingColumns.remove(directive);
        if (status != NOT_SORTED)
            sortingColumns.add(new Directive(column, status));
        sortingStatusChanged();
    }

    protected Icon getHeaderRendererIcon(int column, int size) {
        Directive directive = getDirective(column);
        if (directive == EMPTY_DIRECTIVE)
            return null;
        return new Arrow(directive.direction == DESCENDING, (int) (size * 1.3f), sortingColumns.indexOf(directive));
    }

    private void cancelSorting() {
        sortingColumns.clear();
        sortingStatusChanged();
    }

    public void setColumnComparator(Class type, Comparator<Object> comparator) {
        if (comparator == null)
            columnComparators.remove(type);
        else
            columnComparators.put(type, comparator);
    }

    protected Comparator<Object> getComparator(int column) {
        Class columnType = tableModel.getColumnClass(column);
        Comparator<Object> comparator = columnComparators.get(columnType);
        if (comparator != null)
            return comparator;
        if (Comparable.class.isAssignableFrom(columnType))
            return COMPARABLE_COMPARATOR;
        return LEXICAL_COMPARATOR;
    }

    private Row[] getViewToModel() {
        if (viewToModel == null) {
            int tableModelRowCount = tableModel.getRowCount();
            viewToModel = new Row[tableModelRowCount];
            for (int row = 0; row < tableModelRowCount; row++)
                viewToModel[row] = new Row(row);

            if (isSorting())
                Arrays.sort(viewToModel);
        }
        return viewToModel;
    }

    public int modelIndex(int viewIndex) {
        return getViewToModel()[viewIndex].modelIndex;
    }

    private int[] getModelToView() {
        if (modelToView == null) {
            int n = getViewToModel().length;
            modelToView = new int[n];
            for (int i = 0; i < n; i++)
                modelToView[modelIndex(i)] = i;
        }
        return modelToView;
    }

    // TableModel interface methods
    public int getRowCount() {
        return (tableModel == null) ? 0 : tableModel.getRowCount();
    }

    public int getColumnCount() {
        return (tableModel == null) ? 0 : tableModel.getColumnCount();
    }

    public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }

    public Class getColumnClass(int column) {
        return tableModel.getColumnClass(column);
    }

    public boolean isCellEditable(int row, int column) {
        return tableModel.isCellEditable(modelIndex(row), column);
    }

    public Object getValueAt(int row, int column) {
        return tableModel.getValueAt(modelIndex(row), column);
    }

    public void setValueAt(Object aValue, int row, int column) {
        tableModel.setValueAt(aValue, modelIndex(row), column);
    }

    // Helper classes
    private class Row implements Comparable<Row> {

        private int modelIndex;

        public Row(int index) {
            this.modelIndex = index;
        }

        public int compareTo(Row o) {
            int row1 = modelIndex;
            int row2 = o.modelIndex;

            for (Iterator<Directive> it = sortingColumns.iterator(); it.hasNext();) {
                Directive directive = it.next();
                int column = directive.column;

                Object o1 = tableModel.getValueAt(row1, column);
                Object o2 = tableModel.getValueAt(row2, column);

                int comparison = 0;
                // Define null less than everything, except null.
                if (o1 == null && o2 == null)
                    comparison = 0;
                else if (o1 == null)
                    comparison = -1;
                else if (o2 == null)
                    comparison = 1;
                else
                    comparison = getComparator(column).compare(o1, o2);
                if (comparison != 0)
                    return directive.direction == DESCENDING ? -comparison : comparison;
            }
            return 0;
        }
    }

    private class TableModelHandler implements TableModelListener {

        public void tableChanged(TableModelEvent e) {
            // If we're not sorting by anything, just pass the event along.
            if (!isSorting()) {
                clearSortingState();
                fireTableChanged(e);
                return;
            }

            // If the table structure has changed, cancel the sorting; the
            // sorting columns may have been either moved or deleted from
            // the model.
            if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                cancelSorting();
                fireTableChanged(e);
                return;
            }

            // We can map a cell event through to the view without widening
            // when the following conditions apply:
            //
            // a) all the changes are on one row (e.getFirstRow() == e.getLastRow()) and,
            // b) all the changes are in one column (column != TableModelEvent.ALL_COLUMNS) and,
            // c) we are not sorting on that column (getSortingStatus(column) == NOT_SORTED) and,
            // d) a reverse lookup will not trigger a sort (modelToView != null)
            //
            // Note: INSERT and DELETE events fail this test as they have column == ALL_COLUMNS.
            //
            // The last check, for (modelToView != null) is to see if modelToView
            // is already allocated. If we don't do this check; sorting can become
            // a performance bottleneck for applications where cells
            // change rapidly in different parts of the table. If cells
            // change alternately in the sorting column and then outside of
            // it this class can end up re-sorting on alternate cell updates -
            // which can be a performance problem for large tables. The last
            // clause avoids this problem.
            int column = e.getColumn();
            if (e.getFirstRow() == e.getLastRow() && column != TableModelEvent.ALL_COLUMNS && getSortingStatus(column) == NOT_SORTED && modelToView != null) {
                int viewIndex = getModelToView()[e.getFirstRow()];
                fireTableChanged(new TableModelEvent(TableSorter.this,
                        viewIndex, viewIndex,
                        column, e.getType()));
                return;
            }

            // Something has happened to the data that may have invalidated the row order.
            clearSortingState();
            fireTableDataChanged();
            return;
        }
    }

    private class MouseHandler extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                JTableHeader h = (JTableHeader) e.getSource();
                TableColumnModel columnModel = h.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = columnModel.getColumn(viewColumn).getModelIndex();
                if (column != -1) {
                    int status = getSortingStatus(column);
                    if (!e.isControlDown())
                        cancelSorting();
                    if (status != 0)
                        status *= -1;
                    else
                        status = (e.isShiftDown() ? -1 : 1);
//                // Cycle the sorting states through {NOT_SORTED, ASCENDING, DESCENDING} or
//                // {NOT_SORTED, DESCENDING, ASCENDING} depending on whether shift is pressed.
//                status = status + (e.isShiftDown() ? -1 : 1);
//                status = (status + 4) % 3 - 1; // signed mod, returning {-1, 0, 1}
                    setSortingStatus(column, status);
                }
            } else if (e.getButton() == MouseEvent.BUTTON3)
                ((SelectableColumns) tableModel).selectVisibleColumns(e, new Closure() {

                    public void exec(Object data) {
                        hack_for_table_columns_update.hack_for_table_has_changed();
                    }
                });
        }
    }

    private static class Arrow implements Icon {

        private boolean descending;
        private int size;
        private int priority;

        public Arrow(boolean descending, int size, int priority) {
            this.descending = descending;
            this.size = size;
            this.priority = priority;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color color = c == null ? Color.DARK_GRAY : c.getBackground();
            // In a compound sort, make each succesive triangle 20%
            // smaller than the previous one.
            int dx = (int) (size * 7 / 11 * Math.pow(0.8, priority));
            int dy = descending ? dx : -dx;
            // Align icon (roughly) with font baseline.
            y = y + 5 * size / 6 + (descending ? -dy : 0);

            g.translate(x, y);

            Graphics2D g2d = (Graphics2D) g;
            RenderingHints rhints = g2d.getRenderingHints();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.GRAY);
            int[] px = {dx / 2, 0, dx};
            int[] py = {dy, 0, 0};
            g.fillPolygon(px, py, 3);
            g2d.setRenderingHints(rhints);

            g.setColor(color);
            g.translate(-x, -y);
        }

        public int getIconWidth() {
            return size;
        }

        public int getIconHeight() {
            return size;
        }
    }

    private class SortableHeaderRenderer extends JPanel implements TableCellRenderer {

        private final JLabel text;
        private final JLabel icon;
        private final Color darkc = new Color(0xafafaf);
        private final Color lightc = new Color(0xefefef);
        private final Color transp = new Color(0, 0, 0, 0);

        {
            setLayout(new BorderLayout());
            text = new JLabel();
            text.setFont(UIManager.getFont("Table.font").deriveFont(Font.PLAIN, 11.0f));
            text.setBackground(transp);

            icon = new JLabel();
            icon.setBackground(transp);

            add(text, BorderLayout.CENTER);
            add(icon, BorderLayout.EAST);
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (row < 0)
                return this;
            text.setText(value.toString());
            int modelColumn = table.convertColumnIndexToModel(column);
            icon.setIcon(getHeaderRendererIcon(modelColumn, 12));
            return this;
        }
        private GradientPaint gp;

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            if (gp == null)
                gp = new GradientPaint(0, 0, darkc, 0, getHeight(), lightc);
            Paint pnt = g2.getPaint();
            g2.setPaint(gp);
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(darkc);
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);
            g2.setPaint(pnt);
        }
    }

    private static class Directive {

        private int column;
        private int direction;

        public Directive(int column, int direction) {
            this.column = column;
            this.direction = direction;
        }
    }
}
