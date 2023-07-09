import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;

public class CurveSystem extends JPanel implements Runnable {
    // Runnable 是实现多线程的一种方式
    private static Lock lock = new ReentrantLock();
    private static final int WIDTH = 800;           // 定义窗口宽度
    private static final int HEIGHT = 600;          // 定义窗口高度
    private static final int DELAY = 50;            // 定义每次更新的延迟时间（毫秒）
    private static final int MAX_X = 800;          // 定义x轴最大值
    private static final int MIN_X = -800;         // 定义x轴最小值
    private static final int MAX_Y = 600;           // 定义y轴最大值
    private static final int MIN_Y = -600;          // 定义y轴最小值
    private static final int STEP = 1;              // 定义x轴每个步长的距离
    private static final Color[] COLORS = {Color.BLUE, Color.GREEN, Color.magenta, Color.CYAN};  // 定义每种曲线的颜色
    private static final String[] CURVE_NAMES = {"sin(x)", "cos(x)", "x", "x^2"};   // 定义每种曲线的名称
    private static final int[] CURVE_TYPES = {0, 1, 2, 3};                          // 定义每种曲线的类型编号
    private int currentCurveIndex = 0;            // 当前显示的曲线编号
    private List<Double> xValues = new ArrayList<>();     // 存储x轴上的值
    private List<Double> yValues = new ArrayList<>();     // 存储y轴上的值
    private Thread thread;                         // 线程对象
    private boolean running = false;               // 是否运行标志
    private int scroll = 0;                        // x轴滚动条的位置

    public CurveSystem() {
        setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));      // 设置窗口大小
//        setBackground(Color.WHITE);                                   // 设置背景颜色为白色
        for (int i = MIN_X; i <= MAX_X; i += STEP) {                   // 初始化x轴上的值
            xValues.add((double) i);
            yValues.add(0.0);
        }
        JButton sinButton = new JButton("sin(x)");                    // 创建sin(x)按钮
        sinButton.addActionListener(e -> currentCurveIndex = 0);      // 添加按钮点击事件
        JButton cosButton = new JButton("cos(x)");                    // 创建cos(x)按钮
        cosButton.addActionListener(e -> currentCurveIndex = 1);      // 添加按钮点击事件
        JButton linearButton = new JButton("x");                       // 创建x按钮
        linearButton.addActionListener(e -> currentCurveIndex = 2);   // 添加按钮点击事件
        JButton quadraticButton = new JButton("x^2");                  // 创建x^2按钮
        quadraticButton.addActionListener(e -> currentCurveIndex = 3); // 添加按钮点击事件
        JButton resetButton = new JButton("Reset x");                  // 创建重置x按钮
        resetButton.addActionListener(e -> scroll = 0);               // 添加按钮点击事件
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        startButton.addActionListener((e) -> {
            start();
            stopButton.setEnabled(true);
            startButton.setEnabled(false);
        });
        stopButton.addActionListener((e) -> {
            stop();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        });               // 添加按钮点击事件

        startButton.setEnabled(false);
        add(sinButton);                                                // 将按钮添加到面板中
        add(cosButton);
        add(linearButton);
        add(quadraticButton);
        add(resetButton);                                              // 将重置x按钮添加到面板中
        add(stopButton);
        add(startButton);

        setFocusable(true);                                            // 设置焦点
    }

    private void updateData() {                                            // 更新面板中的数据
        lock.lock();
        System.out.println("update start");
        for (int i = 0; i < xValues.size(); i++) {                     // 遍历x轴上的值
            double x = xValues.get(i) + scroll;                        // 获取当前x轴上的值
            double y = 0.0;
            switch (CURVE_TYPES[currentCurveIndex]) {                  // 根据当前曲线编号计算y轴上的值
                case 0:
                    y = - Math.sin((x+scroll) / 50.0) * 100.0;
                    break;
                case 1:
                    y = - Math.cos((x+scroll) / 50.0) * 100.0;
                    break;
                case 2:
                    y = -(x+scroll);
                    break;
                case 3:
                    y = -(x+scroll)*(x+scroll)/ 100.0;
                    break;
            }
            yValues.set(i, y);                                        // 将计算得到的y轴上的值存储到yValues列表中
        }
        scroll += 2;                                                   // 每次更新后将x轴向右滚动2个像素
        System.out.println("update end");
        lock.unlock();
    }

//        // 添加方法以移动整个图像
//    public void moveImage(int x, int y) {
//        scroll += x; // 改变当前x值以移动整个图像
//    }

    public void start() {                                              // 启动线程的方法
        System.out.println("click start");
        thread = new Thread(this);                                    // 创建线程对象
        running = true;                                               // 设置运行标志为true
        thread.start();                                               // 启动线程
    }

    public void stop() {                                               // 停止线程的方法
        System.out.println("click stop");
        running = false;                                              // 设置运行标志为false
        try {
            thread.join();                                            // 等待线程结束
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {                                                // 线程的运行方法
        System.out.println("start run");
        while (running) {                                             // 如果运行标志为true，则循环更新和重绘面板

            updateData();   // 更新面板中的数据

            repaint();

            try {
                System.out.println("run delay start");
                Thread.sleep(DELAY);                                  // 线程休眠一段时间
                System.out.println("run delay end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("stop run");
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        lock.lock();
        System.out.println("paintComponent start");
        super.paintComponent(graphics); // 父类的绘制操作得以执行

        graphics.setColor(Color.BLACK); // 设置线条颜色为黑色
        graphics.drawLine(0, HEIGHT / 2, WIDTH, HEIGHT / 2); // 画x轴
        graphics.drawString("X", WIDTH - 20, HEIGHT/2 + 10);

        graphics.setColor(Color.RED);
        graphics.drawString("x value", WIDTH / 2 - 40, HEIGHT/2 + 10);
        graphics.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT); // 画y轴

        graphics.setColor(Color.BLACK);
        graphics.drawString("Y", WIDTH / 2 + 10 - scroll, 50);
        graphics.drawString("0", WIDTH / 2 - 10 - scroll, HEIGHT/2 + 10);
        graphics.drawLine(WIDTH / 2 - scroll, 0, WIDTH / 2 - scroll, HEIGHT); // 画y轴

        graphics.setColor(COLORS[currentCurveIndex]); // 设置线条颜色为当前曲线对应的颜色
        for (int i = 0; i < xValues.size() - 1; i++) { // 遍历所有点
            double x1 = xValues.get(i);
            double y1 = yValues.get(i);
            double x2 = xValues.get(i + 1);
            double y2 = yValues.get(i + 1);
            // 将点的坐标转换为在画布上的坐标
            int screenX1 = (int) ((x1 - MIN_X) / (MAX_X - MIN_X) * WIDTH);
            int screenY1 = (int) ((y1 - MIN_Y) / (MAX_Y - MIN_Y) * HEIGHT);
            int screenX2 = (int) ((x2 - MIN_X) / (MAX_X - MIN_X) * WIDTH);
            int screenY2 = (int) ((y2 - MIN_Y) / (MAX_Y - MIN_Y) * HEIGHT);
            // 在画布上画出连线
            graphics.drawLine(screenX1, screenY1, screenX2, screenY2);
        }
        // 在画布上显示当前曲线名称、当前x值、当前y值和y轴缩放值
        graphics.setColor(Color.BLACK);
        graphics.drawString("curve: " + CURVE_NAMES[currentCurveIndex], 10, 20);
        graphics.drawString("x value: " + (scroll / STEP * 2 - 4), 10, 40);
        graphics.drawString("y value: " + (-yValues.get((MAX_X-MIN_X) / STEP / 2)), 10, 60);
        System.out.println("paintComponent end");
        lock.unlock();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Curve System"); // 创建JFrame窗口
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭时退出程序

        CurveSystem curveSystem = new CurveSystem(); // 创建曲线系统对象
        frame.add(curveSystem); // 将曲线系统对象添加到窗口中

        frame.pack(); // 调整窗口大小以适应曲线系统的大小
        curveSystem.start(); // 启动曲线系统
        frame.setVisible(true); // 显示窗口

    }

}



