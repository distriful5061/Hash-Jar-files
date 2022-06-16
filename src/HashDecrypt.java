import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main {
    private static String StringFormat = "%040x";
    private static final String[] HashTypes = {"MD5","SHA-1","SHA-256","SHA-512","SHA3-256","SHA3-512"};
    public static String HASHTYPE;
    public static final char[] Chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            ',','.','/','\\',';',':',']','[','@','^','-',
            '<','>','?','_','}','{','+','*','`','|','~','=',
            '!','\"','#','$','%','&','\'','(',')'};
    public static String CrackString;
    public static int endStringLength;


    public static void main(String[] args) throws NoSuchAlgorithmException {
        Scanner scanner = new Scanner(System.in);
        print("解析する文字列を入力してください:");
        CrackString = scanner.next().toLowerCase();

        if(CrackString.length() == 32){
            CrackString = "00000000"+CrackString;
        }

        print("レインボーテーブルチェックを実行しますか？ Y(es),N(o):");
        String userainbowtable = scanner.next().toLowerCase();
        if(userainbowtable.equals("yes") || userainbowtable.equals("y") || userainbowtable.equals("true")){
            String rainbowCheck = rainbowtableCheck(CrackString);
            if(Objects.equals(rainbowCheck, "")){
                print("どうやら見つからなかったようです。申し訳ありません。\n");
            } else {
                String[] rainbowtableCheckList = rainbowCheck.split("\t");
                print("見つかりました。 "+rainbowtableCheckList[0]+"です\n使用されている方式は、 "+rainbowtableCheckList[1]+" でした。");
                return;
            }
        }
        print("ハッシュ方式を指定してください(先頭の数字を入力してください)\n");

        String suiteiHashType = switch (CrackString.replace("0000","").length()) {
            case 32 -> " MD5:1";
            case 40 -> " SHA-1:2";
            case 64 -> " SHA-256:3, SHA3-256:5";
            case 128 -> " SHA-512:4, SHA3-512:6";
            default -> "見つかりませんでした";
        };

        print("プログラムが推定したハッシュ方式:"+suiteiHashType+"\n");
        int S = 1;
        for(String hashtype : HashTypes){
            print(S+":"+hashtype+"\n");
            S++;
        }
        print(">");
        HASHTYPE = HashTypes[Integer.parseInt(scanner.next()) - 1];
        if(HASHTYPE.equals(HashTypes[0])) StringFormat = "%020x";
        print("最大の原文パスワードの長さを指定してください:");
        endStringLength = Integer.parseInt(scanner.next());

        MessageDigest UsingHashInstance = MessageDigest.getInstance(HASHTYPE);
        int[] RESULTPASSWORD = new int[endStringLength+1];
        for(int i=0;i<endStringLength;i++){
            RESULTPASSWORD[i] = 0;
        }

        long startedMill = System.currentTimeMillis();
        String ProgressBar = "-";
        for(int i=0;i<endStringLength;i++){
            boolean FLAG_1 = false;
            while(!FLAG_1){
                switch (ProgressBar) {
                    case "-" -> ProgressBar = "\\";
                    case "\\" -> ProgressBar = "|";
                    case "|" -> ProgressBar = "/";
                    case "/" -> ProgressBar = "-";
                }

                RESULTPASSWORD[0] = RESULTPASSWORD[0] + 1;
                for(int j=0;j<endStringLength;j++){
                    if(RESULTPASSWORD[j] >= Chars.length){
                        RESULTPASSWORD[j] = 0;
                        RESULTPASSWORD[j+1] = RESULTPASSWORD[j+1] + 1;
                        if((j+1) == (i+1)) FLAG_1 = true;
                    }
                }
                StringBuilder string = new StringBuilder();
                for(int j=0;j<=i;j++){
                    string.append(Chars[RESULTPASSWORD[j]]);
                }
                String result = string.reverse().toString();

                String ResultHashed = Tohash(UsingHashInstance,result);
                System.out.print("\rProcessing "+ProgressBar+" "+result+" "+(i+1)+"/"+endStringLength);
                if(CrackString.equals(ResultHashed)){
                    System.out.println("\nResult:"+result);
                    long endedtime = System.currentTimeMillis() - startedMill;
                    print("\nUp time "+(int) Math.ceil((endedtime / 1000) / 60)+" Minutes, "+(int) Math.ceil(endedtime / 1000)+" Seconds, "+(endedtime % 1000)+" Mill seconds\n");
                    return;
                }
            }
            //System.out.println("ok");

            for(int u=0;u<endStringLength;u++){
                RESULTPASSWORD[u] = 0;
            }
        }
    }

    private static void print(String string){
        System.out.print(string);
    }

    private static String Tohash(MessageDigest UsingHashInstance,String content){
        byte[] HashedByte = UsingHashInstance.digest(content.getBytes());
        return String.format(StringFormat,new BigInteger(1,HashedByte));
    }

    private static String rainbowtableCheck(String hash) throws NoSuchAlgorithmException {
        List<String> hashtypeE = new ArrayList<>();
        List<String> HASHTYPES = new ArrayList<>(List.of(HashTypes));
        switch (CrackString.replace("0000","").length()) {
            case 32 -> {
                hashtypeE.add("MD5");
                HASHTYPES.remove("MD5");
            }
            case 40 -> {
                hashtypeE.add("SHA-1");
                HASHTYPES.remove("SHA-1");
            }
            case 64 -> {
                hashtypeE.add("SHA-256");
                hashtypeE.add("SHA3-256");
                HASHTYPES.remove("SHA-256");
                HASHTYPES.remove("SHA3-256");
            }
            case 128 -> {
                hashtypeE.add("SHA-512");
                hashtypeE.add("SHA3-512");
                HASHTYPES.remove("SHA-512");
                HASHTYPES.remove("SHA3-512");
            }
            default -> {
                for (String hashtype : HashTypes) {
                    for (String str : usefulpassword.split("\n")) {
                        MessageDigest MD = MessageDigest.getInstance(hashtype);
                        String hashed = Tohash(MD, str);
                        if(hash.equals(hashed)) return str+"\t"+hashtype;
                        hashed = Tohash(MD,str.toUpperCase());
                        if(hash.equals(hashed)) return str+"\t"+hashtype;
                    }
                }
                return "";
            }
        }


        for(String hashtype : hashtypeE){
            for(String str : usefulpassword.split("\n")){
                MessageDigest MD = MessageDigest.getInstance(hashtype);
                String hashed = Tohash(MD,str);
                if(hash.equals(hashed)) return str+"\t"+hashtype;
                hashed = Tohash(MD,str.toUpperCase());
                if(hash.equals(hashed)) return str+"\t"+hashtype;
            }
        }


        for(String hashtype : HASHTYPES){
            for(String str : usefulpassword.split("\n")){
                MessageDigest MD = MessageDigest.getInstance(hashtype);
                String hashed = Tohash(MD, str);
                if(hash.equals(hashed)) return str+"\t"+hashtype;
                hashed = Tohash(MD,str.toUpperCase());
                if(hash.equals(hashed)) return str+"\t"+hashtype;
            }
        }
        return "";
    }

    private static final String usefulpassword = """
            123456
            12345
            123456789
            password
            iloveyou
            princess
            1234567
            12345678
            abc123
            nicole
            daniel
            babygirl
            monkey
            lovely
            jessica
            654321
            michael
            root
            qwerty
            111111
            fucku1
            000000
            admin
            app
            chicken""";
}