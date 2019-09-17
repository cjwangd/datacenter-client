import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int lines = s.nextInt();
        int charmax = s.nextInt();
        while (true) {
            if (!s.hasNext() || "".equals(s.nextLine())) {
                break;
            }
            int i = 0;
            while (i < lines) {
                String str = s.nextLine();
                System.out.println(getlines(str, charmax));
                i++;
            }

        }
    }


    static int getlines(String str,int max){
        if (str.length() < max) {
            return 1;
        }else{
            int mc = max;
            while (str.charAt(max - 1) != ' ') {
                mc--;
            }
            return 1+getlines(str.substring(0,mc),max);
        }
    }

}