public class exam {
    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        fbnq(50);
    }
    static long fbnq(int n){
        long result =n<3?1:fbnq(n-1)+fbnq(n-2);
        System.out.println(result);
        return result;
    }
}
