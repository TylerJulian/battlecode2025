package v0;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import v0.util.FastIterableIntSet;

/*
Functions related to the shared array.

 */
public class comms{

    private static RobotController rc;

    private static final int ARRAY_SIZE = 4;

    private static int[] shared_array_buf = new int[ARRAY_SIZE];
    private static FastIterableIntSet shared_array_changed_indexes = new FastIterableIntSet(ARRAY_SIZE);

    public static MapLocation[] allied_spawn_locations = {null, null, null};
    public static MapLocation[] shitter_spawn_locations = {null, null, null};

    public static int[] squadLeaders = {0, 0, 0};
    public static MapLocation[] squadTargets = {null, null, null};

    public static void init(RobotController r)
    {
        rc = r;
    }

    public static void get_shared_array() throws  GameActionException
    {
        int readValue = 0;
        for (int i = 0; i < ARRAY_SIZE; i++)
        {
            readValue = 0; // TODO: fix this //rc.readSharedArray(i);
            if (readValue != shared_array_buf[i])
            {
                if (i < 3) // Squad leaders
                {
                    squadLeaders[i] = readValue;
                } else if (i < 6) { // squad targets
                    squadTargets[i-3] = int_to_location(readValue);
                }
                //TODO set flags for changed values
                shared_array_buf[i] = readValue;
            }
        }
    }

    public static void update_shared_array_from_buffer() throws GameActionException
    {
        if (shared_array_changed_indexes.size > 0) {
            shared_array_changed_indexes.updateIterable();
            int[] indexes = shared_array_changed_indexes.ints;
            for (int i = shared_array_changed_indexes.size; --i >= 0; )
            {
                int oldInt =0; // TODO: fix this //rc.readSharedArray(indexes[i]);
//                if (rc.canWriteSharedArray(indexes[i], shared_array_buf[indexes[i]])){ // TODO: fix this //
//                    rc.writeSharedArray(indexes[i], shared_array_buf[indexes[i]]);
//                }
            }
            shared_array_changed_indexes.clear();
        }
    }

    public static void update_squad_target(int squad_id, MapLocation newTarget) throws GameActionException
    {
        squadTargets[squad_id] = newTarget;
        write_bits((squad_id + 3) * 16, 16, location_to_int(newTarget) );
        update_shared_array_from_buffer(); // TODO put at end of duck's turn
    }

    public static int read_bits(int index, int len)
    {
        int end_bit = index + len - 1;
        int retVal = 0;
        while (index <= end_bit)
        {
            int curr_index = Math.min(index | 0x0F, end_bit);
            retVal = (retVal << (curr_index - index + 1)) + ((shared_array_buf[index/16] % (1<<(16-index % 16))) >> (15 - end_bit % 16));
            index = curr_index + 1;
        }
        return retVal;
    }

    public static int write_bits(int startingBitIndex, int length, int value) {
        assert value < (1 << length);
        int current_length = length;
        while (current_length > 0) {
            int current_ending = startingBitIndex + current_length - 1;
            int len = Math.min(current_ending % 16 + 1, current_length);
            int left = current_ending - len + 1;
            int original_value = (shared_array_buf[left / 16] % (1 << (16 - left % 16))) >> (15 - current_ending % 16);
            int new_value = value % (1 << len);
            value >>= len;
            if (new_value != original_value) {
                shared_array_changed_indexes.add(current_ending / 16);
                shared_array_buf[current_ending / 16] ^= (new_value ^ original_value) << (15 - current_ending % 16);
            }
            current_length -= len;
        }
        return 0;
    }



    public static MapLocation int_to_location(int val)
    {
        if (val == 0)
        {
            return null;
        }
        else
        {
            return new MapLocation(val/64 -1, val % 64 -1);
        }
    }

    public static int location_to_int(MapLocation val)
    {
        return ((val.x + 1) * 64) + (val.y +1);
    }

}