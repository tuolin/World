package main;

import noise.SimplexNoise;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.ARBVertexBufferObject.*;

import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: tuolin
 * Date: 12-1-6
 * Time: PM4:45
 * To change this template use File | Settings | File Templates.
 */
public class WorldMain {
    static int SIZE = 128;
    static double feq = .005f;
    static float threadhold = 1.2f;

    //cache management
    static int CACHE_WIDTH = 128 * 3;
    static int CACHE_DEPTH = 256 + 64;

    byte[][] cache_h = new byte[CACHE_DEPTH + 2][];

    //boolean [] row_need = new boolean[CACHE_DEPTH];
    ArrayList<Integer> needed_row = null;
    IntBuffer row_vbo_ids = BufferUtils.createIntBuffer(CACHE_DEPTH);
    int[] quad_cnts = new int[CACHE_DEPTH];

    Camera camera = new Camera(0, 100, 0);
    float movementSpeed = 100f;

    long lastTime = Sys.getTime(); // when the last frame was


    ByteBuffer terrain = ByteBuffer.allocateDirect(SIZE * SIZE * SIZE);

    int cube_cnt = 0;

    Random r = new Random();

    public WorldMain() {
        for (int i = 0; i < CACHE_DEPTH; ++i) {
            //cache_valid[i] = false;
            row_vbo_ids.put(i, -1);
        }
        camera.pitch(30);
    }

    int vbo_cache_addr(int z) {
        int i = z % CACHE_DEPTH;
        i = (i < 0) ? i + CACHE_DEPTH : i;
        return i;
    }

    int height_cache_addr(int z) {
        int i = z % (CACHE_DEPTH + 2);
        i = (i < 0) ? i + CACHE_DEPTH + 2 : i;
        return i;
    }

    void fill2DNoise() {
        terrain.position(0);
        for (int z = 0; z < SIZE; ++z) {
            for (int x = 0; x < SIZE; ++x) {
                double s = 0;
                for (int i = 0; i < 4; ++i) {
                    s += 1.0f / (1 << i) * SimplexNoise.noise(x * feq * (1 << i),
                            z * feq * (1 << i));
                }

                s *= 30;
                cube_cnt++;
                terrain.put((byte) s);
            }
        }
    }

    void fillNoise() {
        terrain.position(0);
        //int rx = r.nextInt();
        //int ry = r.nextInt();
        //int rz = r.nextInt();
        int rx = 0, ry = 0, rz = 0;

        //Console c = System.console();
        //System.out.println(c);
        System.out.println(String.format("%d %d %d", rx, ry, rz));
        for (int z = 0; z < SIZE; ++z) {
            for (int y = 0; y < SIZE; ++y) {
                for (int x = 0; x < SIZE; ++x) {
                    double s = 0;
                    for (int i = 0; i < 5; ++i) {
                        //s+= 1/Math.pow(2,i)* ImprovedNoise.noise((x+rx) * feq * Math.pow(2,i),
                        //        (y+ry) * feq * Math.pow(2,i), (z+rz) * feq * Math.pow(2,i));

                        s += 1 / Math.pow(2, i) * SimplexNoise.noise((x + rx) * feq * Math.pow(2, i),
                                (y + ry) * feq * Math.pow(2, i), (z + rz) * feq * Math.pow(2, i));

                    }

                    s = Math.abs(s);

                    double tt = 3 * Math.pow((60 - y) / 60.0, 3) - 2 * Math.pow((60 - y) / 60.0, 4);
                    s += 1.4 * tt;
                    /*
                    if (y<10) s+=0.5 * Math.log(10-y);
                    else if (y<60) {
                        double t = 5*Math.pow((60-y)/50.0,4)-4*Math.pow((60-y)/50.0,5);
                        s+=0.7 * t;
                    }
                    else if (y<100) s-= 0.5;
                    else if (y<128) s-=0.7;
                    */

                    //System.out.println(s);
                    byte t = (byte) ((s > threadhold) ? 1 : 0);
                    cube_cnt += (t > 0) ? 1 : 0;
                    terrain.put(t);
                }
            }
        }

        System.out.println(terrain.position());
    }


    public static void main(String[] args) {

        WorldMain m = new WorldMain();

        /*
        boolean two_d = true;

        for (int w = 0; w < 1; ++w) {
            m.cube_cnt = 0;
            long s = System.currentTimeMillis();

            if (!two_d) {
                m.fillNoise();
            } else {
                m.fill2DNoise();
            }

            System.out.println("Time: " + (System.currentTimeMillis() - s) + " cubes:" + m.cube_cnt);
        }
        */
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
            m.initGL();
            long s = System.currentTimeMillis();

            /*
            if (!two_d) {
                m.genGeo();
            } else {
                m.genGeo2D();
            }
            System.out.println("GeoTime: " + (System.currentTimeMillis() - s));
            */

        } catch (LWJGLException e) {

            e.printStackTrace();

            System.exit(0);
        }


        Mouse.setGrabbed(true);

        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {

            //long s = System.currentTimeMillis();
            m.updateCamera();
            m.render();
            Display.update();

            m.updateHeight();
            m.defineRows();


            //System.out.println("Render: " + (System.currentTimeMillis() - s));
        }

        Display.destroy();
    }


    private void updateCamera() {
        Vector3f op = camera.position();
        float dx = 0.0f;
        float dy = 0.0f;
        float dt = 0.0f; //length of frame
        long time = 0;

        float mouseSensitivity = 0.05f;
        time = Sys.getTime();
        //System.out.println("Gettime: " + time);
        dt = (float) (time - lastTime) / Sys.getTimerResolution();
        //System.out.println("dt: " + dt);
        lastTime = time;

        //distance in mouse movement from the last getDX() call.
        dx = Mouse.getDX();
        //distance in mouse movement from the last getDY() call.
        dy = Mouse.getDY();

        //controll camera yaw from x movement fromt the mouse
        camera.yaw(dx * mouseSensitivity);
        //controll camera pitch from y movement fromt the mouse
        camera.pitch(dy * mouseSensitivity);

        //when passing in the distance to move
        //we times the movementSpeed with dt this is a time scale
        //so if its a slow frame u move more then a fast frame
        //so on a slow computer you move just as fast as on a fast computer
        if (Keyboard.isKeyDown(Keyboard.KEY_W))//move forward
        {
            camera.forward(movementSpeed * dt);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S))//move backwards
        {
            camera.backward(movementSpeed * dt);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A))//strafe left
        {
            camera.left(movementSpeed * dt);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D))//strafe right
        {
            camera.right(movementSpeed * dt);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
            camera.up(movementSpeed * dt);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
            camera.down(movementSpeed * dt);
        }

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        camera.look();

        invalidateVBOCache(camera.position().z, op.z);
        invalidateHeightCache(camera.position().z, op.z);

        //System.out.println();
        //Display.setTitle(camera.position().toString());
    }

    private void render() {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        //glOrtho(-100, 200, -100, 200, 20000, -20000);
        GLU.gluPerspective(30, 800.0f / 600, 1, 10000);
        glMatrixMode(GL_MODELVIEW);

        //long s = System.currentTimeMillis();

        /*
        glVertexPointer(3, GL_FLOAT, 24, 12);
        glNormalPointer(GL_FLOAT, 24, 0);
        glDrawArrays(GL_QUADS, 0, cube_cnt * 24);
        */

        Vector3f vp = camera.position();

        // 1. find visible depth range
        int startz = (int) Math.floor(vp.getZ());
        int endz = startz - CACHE_DEPTH;  // minus because we are going along z neg direction

        Display.setTitle("String row: " + startz);
        for (int r = startz; r > endz; --r) {
            renderRow(r);
        }

        //System.out.println("frame: " + (System.currentTimeMillis() - s));
        //int e = glGetError();
        //System.out.print(e);
    }

    private void initGL() {
        //To change body of created methods use File | Settings | File Templates.
        IntBuffer b = BufferUtils.createIntBuffer(1);
        glGenBuffersARB(b);
        int id = b.get(0);

        glBindBufferARB(GL_ARRAY_BUFFER_ARB, id);

        float LightAmbient[] = {0.6f, 0.6f, 0.6f, 1.0f};
        float LightDiffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float LightPosition[] = {30.0f, 60.0f, -60.0f, 0.0f};

        FloatBuffer fb = BufferUtils.createFloatBuffer(4);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL11.GL_COLOR_ARRAY);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        fb.put(LightAmbient[0]).put(LightAmbient[1]).put(LightAmbient[2]).put(LightAmbient[3]);
        fb.position(0);
        glLight(GL_LIGHT1, GL_AMBIENT, fb);

        fb.put(LightDiffuse[0]).put(LightDiffuse[1]).put(LightDiffuse[2]).put(LightDiffuse[3]);
        fb.position(0);
        glLight(GL_LIGHT1, GL_DIFFUSE, fb);

        fb.put(LightPosition[0]).put(LightPosition[1]).put(LightPosition[2]).put(LightPosition[3]);
        fb.position(0);
        glLight(GL_LIGHT1, GL_POSITION, fb);
        glEnable(GL_LIGHT1);
        //glEnable(GL_LIGHTING);
        //glNormal3f(0.0f, 0.0f,1.0f);

        glShadeModel(GL_SMOOTH);

        glClearColor(0, 0, 0, 1);

    }

    void renderRow(int z) {
        //boolean render = mapCache(z);
        int id = row_vbo_ids.get(vbo_cache_addr(z));
        int cnt = quad_cnts[vbo_cache_addr(z)];

        if (id != -1) {
            //System.out.println("Row"+z);
            glBindBufferARB(GL_ARRAY_BUFFER_ARB, id);
            glVertexPointer(3, GL_FLOAT, 28, 0);
            glColorPointer(4, GL_FLOAT, 28, 12);
            glDrawArrays(GL_QUADS, 0, cnt * 4);
        } else {
            if (need_print) {
                //System.out.println("req: " + z);
                need_print = false;
            }
            markNeedRow(z);
        }
    }

    boolean need_print = false;

    void invalidateVBOCache(float ez, float sz) {
        int iez = (int) Math.floor(ez);
        int isz = (int) Math.floor(sz);
        int zv = iez - isz;


        if (zv > 0) {
            for (int z = 0; z < zv; ++z) {
                freeVBO(isz + z);
            }
        } else if (zv < 0) {
            //System.out.println(String.format("invalidating: %d - %d", isz, iez));
            for (int z = 0; z < -zv; ++z) {
                freeVBO(isz - z);
            }
            need_print = true;
        }
    }

    void invalidateHeightCache(float ez, float sz) {
        int iez = (int) Math.floor(ez);
        int isz = (int) Math.floor(sz);
        int zv = iez - isz;


        if (zv > 0) {
            for (int z = 0; z < zv; ++z) {
                freeHeight(isz + z);
            }
        } else if (zv < 0) {
            //System.out.println(String.format("invalidating: %d - %d", isz, iez));
            for (int z = 0; z < -zv; ++z) {
                freeHeight(isz - z + 1);
            }
            need_print = true;
        }
    }

    void freeVBO(int z) {
        int a = vbo_cache_addr(z);
        int id = row_vbo_ids.get(a);

        if (id != -1) {
            glDeleteBuffersARB(id);
            row_vbo_ids.put(a, -1);
        }
    }

    void freeHeight(int z) {
        int a = height_cache_addr(z);
        cache_h[a] = null;
    }

    void markNeedRow(int z) {
        if (needed_row == null) {
            needed_row = new ArrayList<Integer>();
        }

        needed_row.add(z);
    }

    void defineRows() {
        int budget = (int) (movementSpeed + 3);

        for (int i = 0; i < budget && i < needed_row.size(); ++i) {
            int z = needed_row.get(i);
            //System.out.println("gen: " + z);
            genRow(z);
        }

        needed_row.clear();
    }

    void genRow(int z) {
        boolean use_h_cache = true;
        //FloatBuffer fb = BufferUtils.createFloatBuffer(CACHE_WIDTH * (4 * 6 * 3) * 4);
        //ArrayList vertices = new ArrayList(CACHE_WIDTH * 4 * 7);
        ArrayList<Square> sq = new ArrayList<Square>();

        for (int x = 0, sx = -CACHE_WIDTH / 2; x < CACHE_WIDTH; ++x, ++sx) {
            byte h = 0;
            if (!use_h_cache) {
                double hs = 0;
                for (int i = 0; i < 5; ++i) {
                    hs += 1.0f / (1 << i) * SimplexNoise.noise(sx * feq * (1 << i),
                            z * feq * (1 << i));
                }

                hs *= 20;

                //h = (h < 0) ? 0 : h;
                hs += 20;
                hs = (hs < 5) ? 5 : hs;

                /*
                if (h>20) {
                    h*=(1-0.2/20*(h-20));
                }
                */

                //h=1;
                h = (byte) hs;
            } else {
                int ha = height_cache_addr(z);
                int pha = height_cache_addr(z + 1);
                int nha = height_cache_addr(z - 1);

                h = cache_h[ha][sx + CACHE_WIDTH / 2 + 1];
                int west_h = cache_h[ha][sx - 1 + CACHE_WIDTH / 2 + 1];
                int east_h = cache_h[ha][sx + 1 + CACHE_WIDTH / 2 + 1];
                int north_h = cache_h[nha][sx + CACHE_WIDTH / 2 + 1];
                int south_h = cache_h[pha][sx + CACHE_WIDTH / 2 + 1];

                genSquares(sq, sx, (int) h, west_h, east_h, north_h, south_h, z);
            }

            //genCube(fb, sx, (int) h, z);
            //genCube(fb, sx, (int) h - 1, z);
        }

        FloatBuffer fb = BufferUtils.createFloatBuffer(sq.size() * 4 * 7);
        for (int i = 0; i < sq.size(); ++i) {
            Square s = sq.get(i);
            fb.put(s.points);
        }

        fb.position(0);

        int a = vbo_cache_addr(z);
        int id = row_vbo_ids.get(a);

        if (id == -1) {
            id = glGenBuffersARB();
            glBindBufferARB(GL_ARRAY_BUFFER_ARB, id);
            glBufferDataARB(GL_ARRAY_BUFFER_ARB, fb, GL_STATIC_DRAW_ARB);
            row_vbo_ids.put(a, id);
            quad_cnts[a] = sq.size();
        } else {
            System.out.println("Something wrong @ " + z);
        }

    }

    void updateHeight() {
        //int sz = (int) (Math.floor(camera.position().getZ()) + 1);
        //int ez = sz - CACHE_DEPTH - 2;
        int sz = (int) Math.floor(camera.position().getZ() + 1);
        int ez = sz - CACHE_DEPTH - 2;
        for (int z = sz; z > ez; --z) {
            int a = height_cache_addr(z);
            if (cache_h[a] == null) {
                byte[] hs = new byte[CACHE_WIDTH + 2];
                for (int x = -CACHE_WIDTH / 2 - 1, xc = 0; xc < CACHE_WIDTH + 2; ++x, ++xc) {
                    double h = 0;
                    for (int i = 0; i < 5; ++i) {
                        h += 20.0f / (1 << i) * SimplexNoise.noise(x * feq * (1 << i),
                                z * feq * (1 << i));
                    }
                    //h *= 20;
                    h += 20;
                    h = (h < 5) ? 5 : h;
                    hs[xc] = (byte) h;
                }
                cache_h[a] = hs;
            }
        }
    }

    private void genGeo() {
        //To change body of created methods use File | Settings | File Templates.
        int i = 0;
        FloatBuffer fb = BufferUtils.createFloatBuffer(cube_cnt * (4 * 6 * 3) * 2);

        int z = 0, y = 0, x = 0;
        try {
            for (z = SIZE - 1; z >= 0; --z) {
                for (y = 0; y < SIZE; ++y) {
                    for (x = 0; x < SIZE; ++x) {
                        if (terrain.get(x + y * SIZE + z * SIZE * SIZE) == 1) {
                            genCube(fb, x, y, z);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("x:" + x + " y:" + y + " z:" + z + "  " + (x + y * SIZE + z * SIZE * SIZE));
        }


        fb.position(0);
        glBufferDataARB(GL_ARRAY_BUFFER_ARB, fb, GL_STATIC_DRAW_ARB);
    }

    private void genGeo2D() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(cube_cnt * 4 * 6 * 3 * 2);
        byte h = 0;
        int z = 0, x = 0;

        for (z = 0; z < SIZE; ++z) {
            for (x = 0; x < SIZE; ++x) {
                h = terrain.get(x + z * SIZE);
                h = (h < 0) ? 0 : h;
                //h=1;
                genCube(fb, x, h, z);

                /*
                float[] vn = {
                        0, 0, 1, x, h, z, 0, 0, 1, x, h + 1, z, 0, 0, 1, x + 1, h + 1, z, 0, 0, 1, x + 1, h, z,
                        1, 0, 0, x + 1, h, z, 1, 0, 0, x + 1, h + 1, z, 1, 0, 0, x + 1, h + 1, z + 1, 1, 0, 0, x + 1, h, z + 1,
                        0, 0, -1, x + 1, h, z + 1, 0, 0, -1, x + 1, h + 1, z + 1, 0, 0, -1, x, h + 1, z + 1, 0, 0, -1, x, h, z + 1,
                        -1, 0, 0, x, h, z, -1, 0, 0, x, h, z + 1, -1, 0, 0, x, h + 1, z + 1, -1, 0, 0, x, h + 1, z,
                        0, 1, 0, x, h + 1, z, 0, 1, 0, x, h + 1, z + 1, 0, 1, 0, x + 1, h + 1, z + 1, 0, 1, 0, x + 1, h + 1, z,
                        0, -1, 0, x, h, z, 0, -1, 0, x + 1, h, z, 0, -1, 0, x + 1, h, z + 1, 0, -1, 0, x, h, z + 1
                };

                //fb.put(vn);
                */
            }
        }

        fb.position(0);
        glBufferDataARB(GL_ARRAY_BUFFER_ARB, fb, GL_STATIC_DRAW_ARB);
    }

    private void genCube(FloatBuffer fb, int x, int y, int z) {
        float[] vn = {
                0, 0, 1, x, y, z, 0, 0, 1, x, y + 1, z, 0, 0, 1, x + 1, y + 1, z, 0, 0, 1, x + 1, y, z,
                1, 0, 0, x + 1, y, z, 1, 0, 0, x + 1, y + 1, z, 1, 0, 0, x + 1, y + 1, z + 1, 1, 0, 0, x + 1, y, z + 1,
                0, 0, -1, x + 1, y, z + 1, 0, 0, -1, x + 1, y + 1, z + 1, 0, 0, -1, x, y + 1, z + 1, 0, 0, -1, x, y, z + 1,
                -1, 0, 0, x, y, z, -1, 0, 0, x, y, z + 1, -1, 0, 0, x, y + 1, z + 1, -1, 0, 0, x, y + 1, z,
                0, 1, 0, x, y + 1, z, 0, 1, 0, x, y + 1, z + 1, 0, 1, 0, x + 1, y + 1, z + 1, 0, 1, 0, x + 1, y + 1, z,
                0, -1, 0, x, y, z, 0, -1, 0, x + 1, y, z, 0, -1, 0, x + 1, y, z + 1, 0, -1, 0, x, y, z + 1
        };

        fb.put(vn);

        /*
        // first quad front
        fb.put(0).put(0).put(1);
        fb.put(x).put(y).put(z);
        fb.put(0).put(0).put(1);
        fb.put(x).put(y + 1).put(z);
        fb.put(0).put(0).put(1);
        fb.put(x + 1).put(y + 1).put(z);
        fb.put(0).put(0).put(1);
        fb.put(x + 1).put(y).put(z);



        //2 the right
        fb.put(1).put(0).put(0);
        fb.put(x + 1).put(y).put(z);
        fb.put(1).put(0).put(0);
        fb.put(x + 1).put(y + 1).put(z);
        fb.put(1).put(0).put(0);
        fb.put(x + 1).put(y + 1).put(z + 1);
        fb.put(1).put(0).put(0);
        fb.put(x + 1).put(y).put(z + 1);

        //3 the back
        fb.put(0).put(0).put(-1);
        fb.put(x + 1).put(y).put(z + 1);
        fb.put(0).put(0).put(-1);
        fb.put(x + 1).put(y + 1).put(z + 1);
        fb.put(0).put(0).put(-1);
        fb.put(x).put(y + 1).put(z + 1);
        fb.put(0).put(0).put(-1);
        fb.put(x).put(y).put(z + 1);

        //4 the left
        fb.put(-1).put(0).put(0);
        fb.put(x).put(y).put(z);
        fb.put(-1).put(0).put(0);
        fb.put(x).put(y).put(z + 1);
        fb.put(-1).put(0).put(0);
        fb.put(x).put(y + 1).put(z + 1);
        fb.put(-1).put(0).put(0);
        fb.put(x).put(y + 1).put(z);

        //5 top
        fb.put(0).put(1).put(0);
        fb.put(x).put(y + 1).put(z);
        fb.put(0).put(1).put(0);
        fb.put(x).put(y + 1).put(z + 1);
        fb.put(0).put(1).put(0);
        fb.put(x + 1).put(y + 1).put(z + 1);
        fb.put(0).put(1).put(0);
        fb.put(x + 1).put(y + 1).put(z);

        //6 bottom
        fb.put(0).put(-1).put(0);
        fb.put(x).put(y).put(z);
        fb.put(0).put(-1).put(0);
        fb.put(x + 1).put(y).put(z);
        fb.put(0).put(-1).put(0);
        fb.put(x + 1).put(y).put(z + 1);
        fb.put(0).put(-1).put(0);
        fb.put(x).put(y).put(z + 1);
        */
    }

    void genSquares(ArrayList<Square> sq, int x, int h, int wh, int eh, int nh, int sh, int z) {
        float full = 0.95f;

        float wc, ec, nc, sc;
        wc = ec = nc = sc = full;

        if (wh > h)
            wc = 0.1f;

        if (eh > h)
            ec = 0.1f;

        if (nh > h)
            nc = 0.1f;

        if (sh > h)
            sc = 0.1f;


        Square top = new Square(x, h, z, Square.Side.TOP,
                new float[]{(wc + nc)/2, (wc + sc)/2, (ec + sc)/2, (ec + nc)/2});

        sq.add(top);

        if (h > wh) {
            int rh = h;
            while (rh > wh) {
                if (Math.abs(rh - wh) >0.001f) {
                    Square ws = new Square(x, rh, z, Square.Side.LEFT,
                            new float[]{0.7f, 0.5f, 0.5f, 0.7f});
                    sq.add(ws);
                } else {
                    Square ws = new Square(x, rh, z, Square.Side.LEFT,
                            new float[]{0.5f, 0.5f, 0.5f, 0.5f});
                    sq.add(ws);
                }

                rh--;
            }
        }

        if (h > eh) {
            int rh = h;
            while (rh > eh) {
                Square es = new Square(x, rh, z, Square.Side.RIGHT,
                        (Math.abs(rh-eh)>0.001f)?new float[]{0.7f, 0.5f, 0.5f, 0.7f}: new float[]{0.5f, 0.5f, 0.5f, 0.5f});

                sq.add(es);
                rh--;
            }
        }

        if (h > nh) {
            int rh = h;
            while (rh > nh) {
                Square ns = new Square(x, rh, z, Square.Side.BACK,
                        (Math.abs(rh-nh)>0.001f)?new float[]{0.7f, 0.5f, 0.5f, 0.7f}: new float[]{0.5f, 0.5f, 0.5f, 0.5f});

                sq.add(ns);
                rh--;
            }
        }

        if (h > sh) {
            int rh = h;
            while (rh > sh) {
                Square ss = new Square(x, rh, z, Square.Side.FRONT,
                        (Math.abs(rh-sh)>0.001f)?new float[]{0.7f, 0.2f, 0.2f, 0.7f}: new float[]{0.6f, 0.3f, 0.3f, 0.6f});

                sq.add(ss);
                rh--;
            }
        }

    }
}
