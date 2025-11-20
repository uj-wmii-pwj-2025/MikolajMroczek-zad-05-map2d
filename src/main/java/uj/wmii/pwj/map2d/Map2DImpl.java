package uj.wmii.pwj.map2d;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Map2DImpl<R, C, V> implements Map2D<R, C, V> {
    private final Map<R, Map<C, V>> map;
    int size;

    public Map2DImpl() {
        map = new HashMap<>();
        size = 0;
    }

    @Override
    public V put(R rowKey, C colKey, V val) {
        if (rowKey == null || colKey == null) throw new NullPointerException();

        Map<C, V> row = map.get(rowKey);
        if (row == null) {
            row = new HashMap<>();
            map.put(rowKey, row);
        }

        V prev = row.put(colKey, val);
        if (prev == null) {
            size++;
        }

        return prev;
    }

    @Override
    public V get(R rowKey, C colKey) {
        Map<C, V> row = map.get(rowKey);
        if (row == null) return null;
        return row.get(colKey);
    }

    @Override
    public V getOrDefault(R rowKey, C columnKey, V defaultValue) {
        Map<C, V> row = map.get(rowKey);
        if (row == null) return defaultValue;
        if (!row.containsKey(columnKey)) return defaultValue;
        return row.get(columnKey);
    }

    @Override
    public V remove(R rowKey, C columnKey) {
        if (rowKey == null || columnKey == null) throw new NullPointerException();

        Map<C, V> row = map.get(rowKey);
        if (row == null) return null;

        V val = row.remove(columnKey);
        if (val == null) return null;

        if (row.isEmpty()) map.remove(rowKey);

        size--;

        return val;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean nonEmpty() {
        return !map.isEmpty();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        map.clear();
        size = 0;
    }

    @Override
    public Map<C, V> rowView(R rowKey) {
        if (rowKey == null) throw new NullPointerException();

        Map<C, V> row = map.get(rowKey);
        if (row == null || row.isEmpty()) return Collections.emptyMap();

        Map<C, V> copy = new HashMap<>(row);
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public Map<R, V> columnView(C columnKey) {
        if (columnKey == null) throw new NullPointerException();

        Map<R, V> result = new HashMap<>();

        for (R row : map.keySet()) {
            Map<C, V> mpRow = map.get(row);
            if (mpRow == null) continue;

            if (mpRow.containsKey(columnKey)) {
                V val = mpRow.get(columnKey);
                result.put(row, val);
            }
        }

        if (result.isEmpty()) return Collections.emptyMap();
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean containsValue(V value) {
        for (R rowKey : map.keySet()) {
            Map<C, V> row = map.get(rowKey);
            if (row == null) continue;
            if (row.containsValue(value)) return true;
        }
        return false;
    }

    @Override
    public boolean containsKey(R rowKey, C columnKey) {
        if (rowKey == null || columnKey == null) throw new NullPointerException();

        Map<C, V> row = map.get(rowKey);
        if (row == null) return false;

        return row.containsKey(columnKey);
    }

    @Override
    public boolean containsRow(R rowKey) {
        if (rowKey == null) throw new NullPointerException();
        return map.containsKey(rowKey);
    }

    @Override
    public boolean containsColumn(C columnKey) {
        if (columnKey == null) throw new NullPointerException();

        for (R row : map.keySet()) {
            Map<C, V> mpRow = map.get(row);
            if (mpRow == null) continue;
            if (mpRow.containsKey(columnKey)) return true;
        }

        return false;
    }

    @Override
    public Map<R, Map<C, V>> rowMapView() {
        if (map.isEmpty()) return Collections.emptyMap();

        Map<R, Map<C, V>> result = new HashMap<>();
        for (Map.Entry<R, Map<C, V>> e : map.entrySet()) {
            Map<C, V> row = e.getValue();
            if (row == null || row.isEmpty()) continue;
            Map<C, V> rowCopy = new HashMap<>(row);
            result.put(e.getKey(), Collections.unmodifiableMap(rowCopy));
        }

        if (result.isEmpty()) return Collections.emptyMap();
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<C, Map<R, V>> columnMapView() {
        if (map.isEmpty()) return Collections.emptyMap();

        Map<C, Map<R, V>> result = new HashMap<>();
        for (R row : map.keySet()) {
            Map<C, V> mpRow = map.get(row);
            if (mpRow == null || mpRow.isEmpty()) continue;

            for (C col : mpRow.keySet()) {
                Map<R, V> tmp = result.get(col);
                if (tmp == null) {
                    tmp = new HashMap<>();
                    result.put(col, tmp);
                }
                tmp.put(row, mpRow.get(col));
            }
        }

        if (result.isEmpty()) return Collections.emptyMap();

        for (Map.Entry<C, Map<R, V>> e : result.entrySet()) {
            e.setValue(Collections.unmodifiableMap(e.getValue()));
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map2D<R, C, V> fillMapFromRow(Map<? super C, ? super V> target, R rowKey) 
    {
        if (target == null || rowKey == null) throw new NullPointerException();

        Map<C, V> row = map.get(rowKey);
        if (row == null || row.isEmpty()) return this;

        for (Map.Entry<C, V> e : row.entrySet()) {
            target.put(e.getKey(), e.getValue());
        }

        return this;
    }

    @Override
    public Map2D<R, C, V> fillMapFromColumn(Map<? super R, ? super V> target, C columnKey) 
    {
        if (target == null || columnKey == null) throw new NullPointerException();

        for (Map.Entry<R, Map<C, V>> entry : map.entrySet()) 
        {
            R rowKey = entry.getKey();
            Map<C, V> row = entry.getValue();
            if (row == null) continue;

            if (row.containsKey(columnKey)) 
            {
                V value = row.get(columnKey);
                target.put(rowKey, value);
            }
        }

        return this;
    }

        @Override
    public Map2D<R, C, V> putAll(Map2D<? extends R, ? extends C, ? extends V> source)
    {
        if (source == null) throw new NullPointerException();

        for (Map.Entry<?, ?> rowEntry : source.rowMapView().entrySet())
        {
            R rowKey = (R) rowEntry.getKey();

            Map<?, ?> row = (Map<?, ?>) rowEntry.getValue();
            
            if (row == null) continue;

            for (Map.Entry<?, ?> cell : row.entrySet())
            {
                C colKey = (C) cell.getKey();
                V value = (V) cell.getValue();
                put(rowKey, colKey, value);
            }
        }

        return this;
    }


    @Override
    public Map2D<R, C, V> putAllToRow(Map<? extends C, ? extends V> source, R rowKey) 
    {
        if (source == null || rowKey == null) throw new NullPointerException();

        for (Map.Entry<? extends C, ? extends V> e : source.entrySet()) 
        {
            put(rowKey, e.getKey(), e.getValue());
        }

        return this;
    }

    @Override
    public Map2D<R, C, V> putAllToColumn(Map<? extends R, ? extends V> source, C columnKey) 
    {
        if (source == null || columnKey == null) throw new NullPointerException();

        for (Map.Entry<? extends R, ? extends V> e : source.entrySet()) 
        {
            put(e.getKey(), columnKey, e.getValue());
        }

        return this;
    }

    @Override
    public <R2, C2, V2> Map2D<R2, C2, V2> copyWithConversion(
            Function<? super R, ? extends R2> rowFunction,
            Function<? super C, ? extends C2> columnFunction,
            Function<? super V, ? extends V2> valueFunction) 
    
    {    
        if (rowFunction == null || columnFunction == null || valueFunction == null)throw new NullPointerException();

        Map2DImpl<R2, C2, V2> result = new Map2DImpl<>();

        for (Map.Entry<R, Map<C, V>> rowEntry : map.entrySet()) 
        {
        
            R rowKey = rowEntry.getKey();
            Map<C, V> row = rowEntry.getValue();
            if (row == null || row.isEmpty()) continue;

            R2 newRowKey = rowFunction.apply(rowKey);

            for (Map.Entry<C, V> cell : row.entrySet()) 
            {
                C colKey = cell.getKey();
                V value = cell.getValue();

                C2 newColKey = columnFunction.apply(colKey);
                V2 newValue = valueFunction.apply(value);

                result.put(newRowKey, newColKey, newValue);
            }
        }
        return result;
    }
}
