package main;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Created by IntelliJ IDEA.
 * User: tuolin
 * Date: 12-1-10
 * Time: PM10:42
 * To change this template use File | Settings | File Templates.
 */
public class Square {
    public enum Side {
        TOP,
        LEFT,
        RIGHT,
        FRONT,
        BACK
    }

    //Vector3f[] ps = new Vector3f[4];
    //Vector4f[] colrs = new Vector4f[4];
    float[] points = null; //new float[3*4];
    //float[] colrs = new float[4*4];

    public Square(int x, int y, int z, Side side, float[] c) {
        switch (side) {
            case TOP:
                //5 top
                //fb.put(x).put(y + 1).put(z);
                //fb.put(x).put(y + 1).put(z + 1);
                //fb.put(x + 1).put(y + 1).put(z + 1);
                //fb.put(x + 1).put(y + 1).put(z);
            {
                float vp[] = {
                        x, y+1, z, c[0], c[0], c[0], c[0],
                        x, y+1, z+1, c[1], c[1], c[1], c[1],
                        x+1, y+1, z+1, c[2], c[2], c[2], c[2],
                        x+1, y+1, z, c[3], c[3], c[3], c[3]
                };
                points = vp;
                //for (int i=0;i<colrs.length;++i) {
                //    colrs[i] = c;
                //}
            }
            break;

            case LEFT:
                //4 the left
//                fb.put(x).put(y + 1).put(z);
//                fb.put(x).put(y).put(z);
//                fb.put(x).put(y).put(z + 1);
//                fb.put(x).put(y + 1).put(z + 1);
            {
                points = new float [] {
                        x, y+1, z, c[0], c[0], c[0], c[0],
                        x, y, z, c[1], c[1], c[1], c[1],
                        x, y, z+1, c[2], c[2], c[2], c[2],
                        x, y+1, z+1, c[3], c[3], c[3], c[3]
                };
            }
            break;

            case RIGHT:
//            //2 the right
//            fb.put(x + 1).put(y + 1).put(z + 1);
//            fb.put(x + 1).put(y).put(z + 1);
//            fb.put(x + 1).put(y).put(z);
//            fb.put(x + 1).put(y + 1).put(z);
            {
                points = new float[] {
                        x+1, y+1, z+1, c[0], c[0], c[0], c[0],
                        x+1, y, z+1, c[1], c[1], c[1], c[1],
                        x+1, y, z, c[2], c[2], c[2], c[2],
                        x+1, y+1, z, c[3], c[3], c[3], c[3]
                };
            }
            break;

            case FRONT:
//            //3 the back
//            fb.put(x).put(y + 1).put(z + 1);
//            fb.put(x).put(y).put(z + 1);
//            fb.put(x + 1).put(y).put(z + 1);
//            fb.put(x + 1).put(y + 1).put(z + 1);
            {
                points = new float[] {
                        x, y+1, z+1, c[0], c[0], c[0], c[0],
                        x, y, z+1, c[1], c[1], c[1], c[1],
                        x+1, y, z+1, c[2], c[2], c[2], c[2],
                        x+1, y+1, z+1, c[3], c[3], c[3], c[3],
                };
            }
            break;

            case BACK:
            // first quad front
//            fb.put(x + 1).put(y + 1).put(z);
//            fb.put(x + 1).put(y).put(z);
//            fb.put(x).put(y).put(z);
//            fb.put(x).put(y + 1).put(z);
            {
                points = new float[] {
                        x+1, y+1, z, c[0], c[0], c[0], c[0],
                        x+1, y, z, c[1], c[1], c[1], c[1],
                        x, y, z, c[2], c[2], c[2], c[2],
                        x, y+1, z, c[3], c[3], c[3], c[3],
                };
            }
            break;
        }

    }
}
