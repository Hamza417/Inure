package app.simple.inure.diffs;

import java.util.ArrayList;

import androidx.recyclerview.widget.DiffUtil;
import kotlin.Pair;

public class BottomMenuDiffCallback extends DiffUtil.Callback {

    private final ArrayList<Pair<Integer, Integer>> oldList;
    private final ArrayList<Pair<Integer, Integer>> newList;

    public BottomMenuDiffCallback(ArrayList<Pair<Integer, Integer>> oldList, ArrayList<Pair<Integer, Integer>> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Compare the first elements of the pairs
        return oldList.get(oldItemPosition).getFirst().equals(newList.get(newItemPosition).getFirst());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Compare the entire pair objects
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
