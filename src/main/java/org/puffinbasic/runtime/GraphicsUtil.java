package org.puffinbasic.runtime;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.puffinbasic.error.PuffinBasicRuntimeError;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.GRAPHICS_ERROR;

class GraphicsUtil {

    static final int MAX_WIDTH = 4000;
    static final int MAX_HEIGHT = 4000;
    private static final int REFRESH_MILLIS = 40;
    private static final int KEY_BUFFER_SIZE = 16;
    static final String PUT_XOR = "XOR";
    private static final String PUT_OR = "OR";
    private static final String PUT_AND = "AND";
    private static final String PUT_PSET = "PSET";

    static class BasicFrame extends JFrame {

        private final DrawingCanvas drawingCanvas;

        BasicFrame(String title, int w, int h, boolean autoRepaint) {
            drawingCanvas = init(title, w, h, autoRepaint);
        }

        DrawingCanvas getDrawingCanvas() {
            return drawingCanvas;
        }

        private DrawingCanvas init(String title, int w, int h, boolean autoRepaint) {
            var drawingCanvas = new DrawingCanvas(w, h, REFRESH_MILLIS, KEY_BUFFER_SIZE);
            add(drawingCanvas);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    drawingCanvas.stopRefresh();
                }
            });
            addKeyListener(new InkeyDlrKeyListener(drawingCanvas));

            setTitle(title);
            // Don't set size here.
            pack();
            setResizable(false);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            if (autoRepaint) {
                drawingCanvas.startRefresh();
            }
            return drawingCanvas;
        }
    }

    static class DrawingCanvas extends JPanel implements ActionListener {

        private final BufferedImage image;
        private final Graphics2D graphics;
        private final Timer timer;
        private final Deque<String> keyBuffer;
        private final int keyBufferSize;
        private final int w;
        private final int h;
        private final int[] clearBuffer;

        DrawingCanvas(int w, int h, int refreshMillis, int keyBufferSize) {
            this.w = w;
            this.h = h;
            this.clearBuffer = new int[w * h];
            Arrays.fill(clearBuffer, 0);
            // Always use setPreferredSize() here.
            setPreferredSize(new Dimension(w, h));
            this.image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
            this.graphics = (Graphics2D) image.getGraphics();
            this.timer = new Timer(refreshMillis, this);
            this.keyBuffer = new ArrayDeque<>();
            this.keyBufferSize = keyBufferSize;
        }

        BufferedImage getImage() {
            return image;
        }

        String takeNextKey() {
            synchronized (keyBuffer) {
                return keyBuffer.isEmpty() ? "" : keyBuffer.removeFirst();
            }
        }

        void addNextKey(String key) {
            synchronized (keyBuffer) {
                var lastKey = !keyBuffer.isEmpty() ? keyBuffer.getLast() : null;
                if (!key.equals(lastKey) && keyBuffer.size() < keyBufferSize) {
                    keyBuffer.add(key);
                }
            }
        }

        void startRefresh() {
            timer.start();
        }

        void stopRefresh() {
            timer.stop();
        }

        Graphics2D getGraphics2D() {
            return graphics;
        }

        private void draw(java.awt.Graphics g) {
            g.drawImage(image, 0, 0, null);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            synchronized (this) {
                draw(g);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            repaint();
        }

        void floodFill(int x, int y, int r, int g, int b) {
            iterativeFloodFill(image, x, y, graphics.getColor(), new Color(r, g, b));
        }

        void point(int x, int y, int r, int g, int b) {
            Color color;
            if (r != -1 && g != -1 && b != -1) {
                color = new Color(r, g, b);
            } else {
                color = graphics.getColor();
            }
            image.setRGB(x, y, color.getRGB());
        }

        void copyGraphicsToArray(int x1, int y1, int x2, int y2, int[] dest) {
            int w = Math.abs(x1 - x2);
            int h = Math.abs(y1 - y2);
            image.getRGB(x1, y1, w, h, dest, 0, w);
        }

        void copyArrayToGraphics(int x, int y, int w, int h, String action, int[] src) {
            if (action.equalsIgnoreCase(PUT_PSET)) {
                image.setRGB(x, y, w, h, src, 0, w);
            } else {
                var copy = image.getRGB(x, y, w, h, null, 0, w);
                if (action.equalsIgnoreCase(PUT_XOR)) {
                    for (int i = 0; i < copy.length; i++) {
                        copy[i] = copy[i] ^ src[i];
                    }
                } else if (action.equalsIgnoreCase(PUT_OR)) {
                    for (int i = 0; i < copy.length; i++) {
                        copy[i] = copy[i] | src[i];
                    }
                } else if (action.equals(PUT_AND)) {
                    for (int i = 0; i < copy.length; i++) {
                        copy[i] = copy[i] & src[i];
                    }
                } else {
                    throw new PuffinBasicRuntimeError(
                            GRAPHICS_ERROR,
                            "Bad PUT action: " + action
                    );
                }
                image.setRGB(x, y, w, h, copy, 0, w);
            }
        }

        void clear() {
            image.setRGB(0, 0, w, h, clearBuffer, 0, w);
        }
    }

    private static class InkeyDlrKeyListener extends KeyAdapter {

        private final DrawingCanvas drawingCanvas;

        InkeyDlrKeyListener(DrawingCanvas drawingCanvas) {
            this.drawingCanvas = drawingCanvas;
        }

        private String getKeyString(KeyEvent e) {
            int charCode = e.getKeyChar();
            int keyCode = e.getKeyCode();
            if (charCode == 65535) {
                return ((char) 0) + String.valueOf((char) keyCode);
            } else {
                return String.valueOf((char) charCode);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            drawingCanvas.addNextKey(getKeyString(e));
        }
    }

    private static long createPoint(int x, int y) {
        return (((long) x) << 32) | y;
    }

    private static int getX(long point) {
        return (int) (point >>> 32);
    }

    private static int getY(long point) {
        return (int) (point & 0xffffffffL);
    }

    private static void iterativeFloodFill(
            BufferedImage image, int px, int py, Color fill, Color boundary)
    {
        var visited = new LongOpenHashSet();
        var queue = new LongArrayFIFOQueue();
        queue.enqueue(createPoint(px, py));

        while (!queue.isEmpty()) {
            long point = queue.dequeueLong();
            int x = getX(point);
            int y = getY(point);
            if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight() || visited.contains(point)) {
                continue;
            }

            var atXY = new Color(image.getRGB(x, y));
            if (atXY.getRed() == boundary.getRed()
                    && atXY.getGreen() == boundary.getGreen()
                    && atXY.getBlue() == boundary.getBlue()) {
                continue;
            }

            if (atXY.getRed() == fill.getRed()
                    && atXY.getGreen() == fill.getGreen()
                    && atXY.getBlue() == fill.getBlue()) {
                continue;
            }

            visited.add(point);
            image.setRGB(x, y, fill.getRGB());
            if (x > 0) {
                var nextC = new Color(image.getRGB(x - 1, y));
                if (nextC.getRed() != fill.getRed()
                        || nextC.getGreen() != fill.getGreen()
                        || nextC.getBlue() != fill.getBlue()) {
                    queue.enqueue(createPoint(x - 1, y));
                }
            }
            if (x < image.getWidth() - 1) {
                var nextC = new Color(image.getRGB(x + 1, y));
                if (nextC.getRed() != fill.getRed()
                        || nextC.getGreen() != fill.getGreen()
                        || nextC.getBlue() != fill.getBlue()) {
                    queue.enqueue(createPoint(x + 1, y));
                }
            }
            if (y > 0) {
                var nextC = new Color(image.getRGB(x, y - 1));
                if (nextC.getRed() != fill.getRed()
                        || nextC.getGreen() != fill.getGreen()
                        || nextC.getBlue() != fill.getBlue()) {
                    queue.enqueue(createPoint(x, y - 1));
                }
            }
            if (y < image.getHeight() - 1) {
                var nextC = new Color(image.getRGB(x, y + 1));
                if (nextC.getRed() != fill.getRed()
                        || nextC.getGreen() != fill.getGreen()
                        || nextC.getBlue() != fill.getBlue()) {
                    queue.enqueue(createPoint(x, y + 1));
                }
            }
        }
    }
}
