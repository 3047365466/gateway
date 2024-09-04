/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/30 7:04
 */
public class test {
    private int a = 2;

    public test() {

    }

    public static void main(String[] args) {
        test2 t = new test2();
        test t1 = t;
        System.out.println(t1.a);
    }
}
class test2 extends test{

    public test2() {
        super();
    }
}
