import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        while (true) {
            List<String> list = new ArrayList<>(10000);
            if (!s.hasNext()) {
                break;
            }
            int line = s.nextInt();
            String strs[] = new String[line];

            int i = 0;
            while (i<line) {
                list.add(s.next());
                i++;
            }
            Collections.sort(list,Collections.reverseOrder());
            System.out.println(list);
        }
    }

}