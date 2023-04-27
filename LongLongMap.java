import sun.misc.Unsafe;

import java.util.Arrays;

/**
 * Map backed up by array with open addressing
 */
class LongLongMap {
    private long ptr;
    private long sz;
    private long length;

    private long NO_VALUE_RESULT = Long.MIN_VALUE;
    private long SPECIAL_VALUE = Long.MAX_VALUE;
    private long REMOVED = Long.MIN_VALUE + 1;

    LongLongMap(long ptr, long sizeInBytes) {
        this.ptr = ptr;
        this.sz = sizeInBytes;
        this.length = sizeInBytes / 16;
        fillWithSpecialValue();
    }

    void fillWithSpecialValue() {
        for (int i = 0; i < sz; i+=8) {
            putLong(ptr + i, SPECIAL_VALUE);
        }
    }

    long put(long key, long value) {
        long pos = hash(key);
        for (;;) {
            long k1 = getLong(pos);
            if (k1 == key) {
                long prevValue = getLong(pos+8);
                putLong(pos+8, value);
                return prevValue;
            }
            if (k1 == REMOVED) {
                putLong(pos, key);
                putLong(pos+8, value);
                return NO_VALUE_RESULT;
            }
            if (k1 == SPECIAL_VALUE) {
                putLong(pos, key);
                putLong(pos+8, value);
                return NO_VALUE_RESULT;
            }
            pos += 16;
        }
    }

    long get(long key) {
        long pos = hash(key);
        while (key != getLong(pos)) {
            if (pos > (ptr + sz) || getLong(pos) == SPECIAL_VALUE || getLong(pos) == REMOVED) {
                return NO_VALUE_RESULT;
            }
            pos += 16;
        }
        return getLong(pos+8);
    }

    long hash(long key) {
        long hash = ptr + (key % length) * 16;
        System.out.println("key=" + key + ", hash=" + hash);
        return hash;
    };

    long remove(long key) {
        long pos = hash(key);
        while (key != getLong(pos)) {
            if (pos > (ptr + sz) || getLong(pos) == SPECIAL_VALUE || getLong(pos) == REMOVED) {
                return NO_VALUE_RESULT;
            }
            pos += 16;
        }
        if (key == getLong(pos)) {
            long oldValue = getLong(pos+8);
            putLong(pos, REMOVED);
            putLong(pos+8, REMOVED);
            return oldValue;
        }
        return NO_VALUE_RESULT;
    }

    protected void putLong(long address, long value) {
        int i = (int)address / 8;
        System.out.println("putLong: i=" + i + ", value=" + value);
        ar[i] = value;
//        Unsafe.getUnsafe().putLong(address, value);
    }

    protected long getLong(long address) {
        int i = (int)address / 8;
        System.out.println("getLong: i=" + i + ", value=" + ar[i]);
        return ar[i];
//        return Unsafe.getUnsafe().getLong(address);
    }

    private static long[] ar = new long[20];

    public static void main(String[] args) {
        LongLongMap map = new LongLongMap(0, 160);
        map.put(52, 149);
        map.put(29, 150);
        map.put(73, 152);
        System.out.println(Arrays.toString(ar));
        map.remove(31);
        map.remove(32);
        map.remove(52);
        System.out.println(Arrays.toString(ar));
        System.out.println(map.get(32));
        System.out.println(map.get(52));
        System.out.println(map.get(29));
        System.out.println(map.get(73));
    }
}