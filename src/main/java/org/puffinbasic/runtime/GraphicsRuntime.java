package org.puffinbasic.runtime;

import it.unimi.dsi.fastutil.ints.IntList;
import org.puffinbasic.domain.PuffinBasicSymbolTable;
import org.puffinbasic.domain.STObjects;
import org.puffinbasic.domain.STObjects.STVariable;
import org.puffinbasic.error.PuffinBasicRuntimeError;
import org.puffinbasic.parser.PuffinBasicIR.Instruction;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.puffinbasic.domain.PuffinBasicSymbolTable.NULL_ID;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.INT32;
import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.GRAPHICS_ERROR;

class GraphicsRuntime {

    private static final int REFRESH_MILLIS = 200;
    private static final int KEY_BUFFER_SIZE = 16;

    static class GraphicsState {
        private BasicFrame frame;

        boolean isInitialized() {
            return frame != null;
        }

        BasicFrame getFrame() {
            assertScreenInitialized();
            return frame;
        }

        Graphics2D getGraphics2D() {
            return getFrame().surface.graphics;
        }

        void setFrame(BasicFrame frame) {
            assertNewScreen();
            this.frame = frame;
        }

        private void assertNewScreen() {
            if (frame != null) {
                throw new PuffinBasicRuntimeError(
                        GRAPHICS_ERROR,
                        "Screen cannot be called again!"
                );
            }
        }

        private void assertScreenInitialized() {
            if (frame == null) {
                throw new PuffinBasicRuntimeError(
                        GRAPHICS_ERROR,
                        "Screen has already been created!"
                );
            }
        }
    }

    public static void screen(
            GraphicsState graphicsState,
            PuffinBasicSymbolTable symbolTable,
            Instruction instr0,
            Instruction instruction)
    {
        var w = symbolTable.get(instr0.op1).getValue().getInt32();
        var h = symbolTable.get(instr0.op2).getValue().getInt32();
        var title = symbolTable.get(instruction.op1).getValue().getString();

        graphicsState.setFrame(new BasicFrame(title, w, h));
        EventQueue.invokeLater(() -> graphicsState.getFrame().setVisible(true));
    }

    public static void end(GraphicsState graphicsState) {
        SwingUtilities.invokeLater(
                () -> {
                    if (graphicsState.isInitialized()) {
                        var frame = graphicsState.getFrame();
                        frame.dispatchEvent(
                                new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    }
                });
    }

    public static void circle(
            GraphicsState graphicsState,
            PuffinBasicSymbolTable symbolTable,
            List<Instruction> instr0,
            Instruction instruction)
    {
        var i0 = instr0.get(0);
        var i1 = instr0.get(1);

        var x = symbolTable.get(i0.op1).getValue().getInt32();
        var y = symbolTable.get(i0.op2).getValue().getInt32();
        Double s = i1.op1 != NULL_ID ? symbolTable.get(i1.op1).getValue().getFloat64() : null;
        Double e = i1.op1 != NULL_ID ? symbolTable.get(i1.op2).getValue().getFloat64() : null;
        int r1 = symbolTable.get(instruction.op1).getValue().getInt32();
        int r2 = symbolTable.get(instruction.op2).getValue().getInt32();

        if (s == null || e == null) {
            graphicsState.getGraphics2D().drawOval(x, y, r1, r2);
        } else {
            graphicsState.getGraphics2D().drawArc(
                    x, y, r1, r2, (int) Math.toDegrees(s) , (int) Math.toDegrees(e)
            );
        }
    }

    public static void line(
            GraphicsState graphicsState,
            PuffinBasicSymbolTable symbolTable,
            List<Instruction> instr0,
            Instruction instruction)
    {
        var i0 = instr0.get(0);
        var i1 = instr0.get(1);

        var x1 = symbolTable.get(i0.op1).getValue().getInt32();
        var y1 = symbolTable.get(i0.op2).getValue().getInt32();
        var x2 = symbolTable.get(i1.op1).getValue().getInt32();
        var y2 = symbolTable.get(i1.op2).getValue().getInt32();
        String bf = instruction.op1 != NULL_ID
                ? symbolTable.get(instruction.op1).getValue().getString()
                : "";

        if (bf.isEmpty()) {
            graphicsState.getGraphics2D().drawLine(x1, y1, x2, y2);
        } else if (bf.equals("B")) {
            graphicsState.getGraphics2D().drawRect(
                    x1, y1, Math.abs(x1 - x2), Math.abs(y1 - y2)
            );
        } else {
            graphicsState.getGraphics2D().fillRect(
                    x1, y1, Math.abs(x1 - x2), Math.abs(y1 - y2)
            );
        }
    }

    public static void color(
            GraphicsState graphicsState,
            PuffinBasicSymbolTable symbolTable,
            Instruction instr0,
            Instruction instruction)
    {
        var r = symbolTable.get(instr0.op1).getValue().getInt32();
        var g = symbolTable.get(instr0.op2).getValue().getInt32();
        var b = symbolTable.get(instruction.op1).getValue().getInt32();

        graphicsState.getGraphics2D().setColor(new Color(r, g, b));
    }


    public static void paint(
            GraphicsState graphicsState,
            PuffinBasicSymbolTable symbolTable,
            List<Instruction> instr0,
            Instruction instruction)
    {
        var i0 = instr0.get(0);
        var i1 = instr0.get(1);

        var r = symbolTable.get(i0.op1).getValue().getInt32();
        var g = symbolTable.get(i0.op2).getValue().getInt32();
        var b = symbolTable.get(i1.op1).getValue().getInt32();
        var x = symbolTable.get(instruction.op1).getValue().getInt32();
        var y = symbolTable.get(instruction.op2).getValue().getInt32();

        graphicsState.getFrame().getSurface().floodFill(x, y, r, g, b);
    }

    public static void pset(
            GraphicsState graphicsState,
            PuffinBasicSymbolTable symbolTable,
            List<Instruction> instr0,
            Instruction instruction)
    {
        var i0 = instr0.get(0);
        var i1 = instr0.get(1);

        var r = i0.op1 != NULL_ID ? symbolTable.get(i0.op1).getValue().getInt32() : -1;
        var g = i0.op2 != NULL_ID ? symbolTable.get(i0.op2).getValue().getInt32() : -1;
        var b = i1.op1 != NULL_ID ? symbolTable.get(i1.op1).getValue().getInt32() : -1;
        var x = symbolTable.get(instruction.op1).getValue().getInt32();
        var y = symbolTable.get(instruction.op2).getValue().getInt32();

        graphicsState.getFrame().getSurface().point(x, y, r, g, b);
    }


    public static void get(
            GraphicsState graphicsState,
            PuffinBasicSymbolTable symbolTable,
            List<Instruction> instr0,
            Instruction instruction)
    {
        var i0 = instr0.get(0);
        var i1 = instr0.get(1);

        var x1 = symbolTable.get(i0.op1).getValue().getInt32();
        var y1 = symbolTable.get(i0.op2).getValue().getInt32();
        var x2 = symbolTable.get(i1.op1).getValue().getInt32();
        var y2 = symbolTable.get(i1.op2).getValue().getInt32();

        var variable = (STVariable) symbolTable.get(instruction.op1);
        if (!variable.getVariable().isArray()
            || variable.getValue().getNumArrayDimensions() != 2
            || variable.getValue().getDataType() != INT32)
        {
            throw new PuffinBasicRuntimeError(
                    GRAPHICS_ERROR,
                    "Bad variable! Expected Int32 2D-Array variable: " + variable.getVariable()
            );
        }
        graphicsState.getFrame().getSurface().copyGraphicsToArray(
                x1, y1, x2, y2, variable.getValue().getInt32Array1D()
        );
    }

    public static void put(
            GraphicsState graphicsState,
            PuffinBasicSymbolTable symbolTable,
            List<Instruction> instr0,
            Instruction instruction)
    {
        var i0 = instr0.get(0);

        var x = symbolTable.get(i0.op1).getValue().getInt32();
        var y = symbolTable.get(i0.op2).getValue().getInt32();
        var action = instruction.op1 != NULL_ID
                ? symbolTable.get(instruction.op1).getValue().getString()
                : "XOR";
        action = action.toUpperCase();

        var variable = (STVariable) symbolTable.get(instruction.op2);
        var value = variable.getValue();
        if (!variable.getVariable().isArray()
                || value.getNumArrayDimensions() != 2
                || value.getDataType() != INT32)
        {
            throw new PuffinBasicRuntimeError(
                    GRAPHICS_ERROR,
                    "Bad variable! Expected Int32 2D-Array variable: " + variable.getVariable()
            );
        }
        var dims = value.getArrayDimensions();
        graphicsState.getFrame().getSurface().copyArrayToGraphics(
                x, y, dims.getInt(0), dims.getInt(1), action, value.getInt32Array1D()
        );
    }

    public static void inkeydlr(
            GraphicsState graphicsState,
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var key = graphicsState.getFrame().getSurface().takeNextKey();
        symbolTable.get(instruction.result).getValue().setString(key);
    }

    private static class Surface extends JPanel implements ActionListener {

        private final BufferedImage image;
        private final Graphics2D graphics;
        private final Timer timer;
        private final Deque<String> keyBuffer;
        private final int keyBufferSize;

        Surface(int w, int h, int refreshMillis, int keyBufferSize) {
            this.image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
            this.graphics = (Graphics2D) image.getGraphics();
            this.timer = new Timer(refreshMillis, this);
            this.keyBuffer = new ArrayDeque<>();
            this.keyBufferSize = keyBufferSize;
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
            draw(g);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            repaint();
        }

        public void floodFill(int x, int y, int r, int g, int b) {
            recursiveFloodFill(image, x, y, graphics.getColor(), new Color(r, g, b));
        }

        public void point(int x, int y, int r, int g, int b) {
            Color color;
            if (r != -1 && g != -1 && b != -1) {
                color = new Color(r, g, b);
            } else {
                color = graphics.getColor();
            }
            image.setRGB(x, y, color.getRGB());
        }

        public void copyGraphicsToArray(int x1, int y1, int x2, int y2, int[] dest) {
            int w = Math.abs(x1 - x2);
            int h = Math.abs(y1 - y2);
            image.getRGB(x1, y1, w, h, dest, 0, w);
        }

        public void copyArrayToGraphics(int x, int y, int w, int h, String action, int[] src) {
            if (action.equalsIgnoreCase("PSET")) {
                image.setRGB(x, y, w, h, src, 0, w);
            } else {
                var copy = image.getRGB(x, y, w, h, null, 0, w);
                if (action.equalsIgnoreCase("XOR")) {
                    for (int i = 0; i < copy.length; i++) {
                        copy[i] = copy[i] ^ src[i];
                    }
                } else if (action.equalsIgnoreCase("OR")) {
                    for (int i = 0; i < copy.length; i++) {
                        copy[i] = copy[i] | src[i];
                    }
                } else {
                    for (int i = 0; i < copy.length; i++) {
                        copy[i] = copy[i] & src[i];
                    }
                }
                image.setRGB(x, y, w, h, copy, 0, w);
            }
        }
    }

    private static void recursiveFloodFill(
            BufferedImage image, int x, int y, Color fill, Color boundary)
    {
        var atXY = new Color(image.getRGB(x, y));
        if (atXY.getRed() == boundary.getRed()
            && atXY.getGreen() == boundary.getGreen()
            && atXY.getBlue() == boundary.getBlue()) {
            return;
        }

        if (atXY.getRed() == fill.getRed()
                && atXY.getGreen() == fill.getGreen()
                && atXY.getBlue() == fill.getBlue()) {
            return;
        }

        image.setRGB(x, y, fill.getRGB());
        if (x > 0) {
            recursiveFloodFill(image, x - 1 , y, fill, boundary);
        }
        if (x < image.getWidth() - 1) {
            recursiveFloodFill(image, x + 1, y, fill, boundary);
        }
        if (y > 0) {
            recursiveFloodFill(image, x , y - 1, fill, boundary);
        }
        if (x < image.getHeight() - 1) {
            recursiveFloodFill(image, x, y + 1, fill, boundary);
        }
    }

    private static class BasicFrame extends JFrame {

        private final Surface surface;

        BasicFrame(String title, int w, int h) {
            surface = init(title, w, h);
        }

        Surface getSurface() {
            return surface;
        }

        private Surface init(String title, int w, int h) {
            var surface = new Surface(w, h, REFRESH_MILLIS, KEY_BUFFER_SIZE);
            add(surface);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    surface.stopRefresh();
                }
            });
            addKeyListener(new InkeyDlrKeyListener(surface));

            setTitle(title);
            setSize(w, h);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            surface.startRefresh();

            return surface;
        }
    }

    private static class InkeyDlrKeyListener extends KeyAdapter {

        private final Surface surface;

        InkeyDlrKeyListener(Surface surface) {
            this.surface = surface;
        }

        private String getKeyString(KeyEvent e) {
            int charCode = e.getKeyChar();
            int keyCode = e.getKeyCode();
            if (charCode == 65535) {
                return "0" + (char) keyCode;
            } else {
                return String.valueOf((char) charCode);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            surface.addNextKey(getKeyString(e));
        }
    }
}
