/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myrtree;

import gnu.trove.TIntProcedure;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yyy
 */
public class AddToListProcedure implements TIntProcedure {

    private List<Integer> m_list = new ArrayList<Integer>();

    @Override
    public boolean execute(int id) {
        m_list.add(new Integer(id));
        return true;
    }

    public List<Integer> getList() {
        return m_list;
    }
}
