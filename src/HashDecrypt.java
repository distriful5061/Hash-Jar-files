import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * 解析用非同期スレッド。インスタンスを作成して使用する
 * @author distriful5061
 * @version 1.0.0
 */
class DecryptThread extends java.lang.Thread {
    /**
     * 終了する、平文パスワードの最大文字数(8の場合9を超えると終了)
     */
    private final int endStringLength;
    /**
     * 解析するハッシュ値を格納する変数
     */
    private final String CrackString;
    /**
     * 解析に使用する、charのリスト
     */
    private final char[] Chars;
    /**
     * 一番最初のスレッドかどうか。trueはThread-1、falseはそれ以外という意味
     */
    private final boolean is1Thread;
    /**
     * 一番最初(一番左)の文字を、Chars変数の何番目から使用するか
     */
    private final int startIndex;
    /**
     * 一番最初(一番左)の文字を、Chars変数のstartIndex変数から何番目までを使用するか
     */
    private final int endIndex;

    /**
     * このクラスのコンストラクター。finalの変数の初期化のために使用される
     * @param endStringLength1 終了する、平文パスワードの最大文字数(8の場合9を超えると終了)
     * @param CrackString1 解析するハッシュ値
     * @param Chars1 解析に使用する、charのリスト
     * @param startIndex1 一番最初(一番左)の文字を、Chars変数の何番目から使用するか
     * @param endIndex1 一番最初(一番左)の文字を、Chars変数のstartIndex変数から何番目までを使用するか
     */
    public DecryptThread(int endStringLength1,String CrackString1,char[] Chars1,int startIndex1,int endIndex1){
        this.endStringLength = endStringLength1;
        this.CrackString = CrackString1;
        this.Chars = Chars1;
        this.startIndex = startIndex1;
        this.endIndex = endIndex1;

        is1Thread = getName().equals("Thread-1");
    }

    /**
     * System.out.printlnをいちいち書くのが面倒だったので、短く書けるように作成したメソッドです。
     * @param content 出力する内容
     */
    private void print(String content){
        System.out.println(content);
    }

    /**
     * 解析用メソッド。MainクラスのOKPw,gotPw, HASHTYPEなどを使用しています。
     * @throws RuntimeException 引き渡されたハッシュ関数名が存在しません。
     */
    public void run(){
        MessageDigest UsingHashInstance; //ハッシュ値計算のための変数

        try {
            UsingHashInstance = MessageDigest.getInstance(Main.HASHTYPE);//Mainクラスで指定されたHASHTYPEを使用
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        int[] RESULT_PASSWORD = new int[endStringLength + 1]; //Chars変数のindexを格納するためのint[]。IndexOutOfRange対策に一桁追加していますが、後述にて+1された場所にアクセスされません、
        Arrays.fill(RESULT_PASSWORD,0); //RESULT-PASSWORDを初期化

        for(int i=0;i<endStringLength;i++){
            if(Main.gotPw) return;//もしパスワードがすでに算出されている場合は終了

            if(is1Thread) print("\rIn "+(i+1)+"/"+endStringLength+"                      ");//もしThread-1なら今どの段階にいるのか出力

            boolean FLAG_1 = false;//次の桁数に進んだことを示すFlag
            while(!FLAG_1 && !Main.gotPw){//FLAGがfalseかつMain.gotPwがfalseならば実行する
                RESULT_PASSWORD[0] = RESULT_PASSWORD[0] + 1;//0桁目(文字で言う一番最後)に+1する
                for(int j=0;j<endStringLength;j++){
                    if(j == i){//もし、ループしている値が現在の最大文字数と同じなら

                        if(RESULT_PASSWORD[j] > endIndex){//もし最大インデックスより大きいならば
                            RESULT_PASSWORD[j] = startIndex;
                            RESULT_PASSWORD[j+1] = RESULT_PASSWORD[j+1] + 1;
                            FLAG_1 = true;//ここで終了
                        }
                    }

                    if(RESULT_PASSWORD[j] >= Chars.length){//現在ループしている値が、Charsの長さと同じまたは大きいならば
                        RESULT_PASSWORD[j] = 0;
                        RESULT_PASSWORD[j+1] = RESULT_PASSWORD[j+1] + 1;

                        if(j == i) FLAG_1 = true;//ループしている値が現在の最大文字数と同じなら終了する
                    }
                }
                StringBuilder string = new StringBuilder();

                for(int j=0;j<=i;j++){//現在の桁数だけループする
                    string.append(Chars[RESULT_PASSWORD[j]]);
                }

                String result = string.reverse().toString();//逆にしてStringに変換

                String ResultHashed = Tohash(UsingHashInstance,result);//Tohashメソッドを使用しハッシュ化
                if(CrackString.equals(ResultHashed)){//もしCrackStringとResultHashedが同じなら
                    Main.gotPw = true;
                    Main.OKPw = result;

                    return;//終了
                }
            }

            Arrays.fill(RESULT_PASSWORD,0); //全桁を初期化
        }
    }

    /**
     * ハッシュ値を返すメソッド。MainクラスのStringFormat変数を使用しています
     * @param UsingHashInstance 使用するハッシュのMessageDigest型変数
     * @param content ハッシュにしたいString型の原文
     * @return ハッシュ化されたString型文字列
     */
    private static String Tohash(MessageDigest UsingHashInstance,String content){
        byte[] HashedByte = UsingHashInstance.digest(content.getBytes());//Byte単位でハッシュ化
        return String.format(Main.StringFormat,new BigInteger(1,HashedByte));//Stringに直す
    }
}

/**
 * 解析スレッドなどを呼び出したり、必要な値を受け取るクラス
 * @author distriful5061
 * @version 1.0.0
 */
public class Main {
    /**
     * 一致したパスワードがあるかどうかを示す変数。trueははい、falseはいいえ
     */
    public static boolean gotPw = false;
    /**
     * 一致したパスワードの平文が代入される変数
     */
    public static String OKPw = "";
    /**
     * ハッシュ値を整形する際に使用するFormat。MD5の場合にのみ%020xになる
     */
    public static String StringFormat = "%040x";
    /**
     * ハッシュ値の方式が格納された変数。MD2のみ解析がうまくできなかったため除外
     */
    private static final String[] HashTypes = {"MD5","SHA-1","SHA-224","SHA-256","SHA-384","SHA-512","SHA3-224","SHA3-256","SHA3-384","SHA3-512"};
    /**
     * 使用されるハッシュ値の値
     */
    public static String HASHTYPE;
    /**
     * String型の解析に使用する文字列のリスト。+が多すぎて正直見にくい
     */
    public static final String Chars =
                     "0"+ "1"+ "2"+ "3"+ "4"+ "5"+ "6"+ "7"+ "8"+ "9"+
                    "a"+ "b"+ "c"+ "d"+ "e"+ "f"+ "g"+ "h"+ "i"+ "j"+
                    "k"+ "l"+ "m"+ "n"+ "o"+ "p"+ "q"+ "r"+ "s"+ "t"+
                    "u"+ "v"+ "w"+ "x"+ "y"+ "z"+
                    "A"+ "B"+ "C"+ "D"+ "E"+ "F"+ "G"+ "H"+ "I"+ "J"+
                    "K"+ "L"+ "M"+ "N"+ "O"+ "P"+ "Q"+ "R"+ "S"+ "T"+
                    "U"+ "V"+ "W"+ "X"+ "Y"+ "Z"+
                    "+"+"."+"/"+"\\"+";"+":"+"]"+"["+"@"+"^"+"-"+
                    "<"+">"+"?"+"_"+"}"+"{"+","+"*"+"`"+"|"+"~"+"="+
                    "!"+"\""+"#"+"$"+"%"+"&"+ "'" +"("+")"+" "+"¯";

    /**
     * Charリスト型の変数。解析に使用するcharが含まれている
     */
    public static final char[] Chars2 = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            ',','.','/','\\',';',':',']','[','@','^','-',
            '<','>','?','_','}','{','+','*','`','|','~','=',
            '!','\"','#','$','%','&','\'','(',')'};

    /**
     * 解析するハッシュ値が格納される変数。長さである程度の予測が可能。
     */
    public static String CrackString;
    /**
     * 終了する、平文パスワードの最大文字数(8の場合9を超えると終了)
     */
    public static int endStringLength;


    /**
     * メインメソッド。解析スレッドの起動や必要なパラメーターをユーザーから受け取っています。
     * @param args args. 起動時に渡される引数の変数。正直使わない。
     * @throws NoSuchAlgorithmException 引き渡されたハッシュ関数名が存在しません。
     * @throws InterruptedException Ctrl+Cなどで動作が中断されました。
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException {
        Scanner scanner = new Scanner(System.in);//scannerインスタンスを作成
        print("解析する文字列を入力してください:");
        CrackString = scanner.next().toLowerCase()
                .replace(" ","")
                .replace("　","")
                .replace("\n","");//半角と全角空白、改行を削除しています。

        if(CrackString.length() == 32){//32文字=MD5だと判別できる
            CrackString = "00000000"+CrackString;//JavaでMD5を作成すると、8つの0が先頭に付加されます
        }

        print("ディクショナリーアタックを実行しますか？ Y(es),N(o):");
        String userainbowtable = scanner.next().toLowerCase();//レインボーチェックを使用するかどうか。

        if(isYes(userainbowtable)){//yesもしくはy、trueならば
            print("ビルトインリストで解析中...\n");
            String rainbowCheck = rainbowtableCheck(CrackString);//rainbowtableCheck関数を呼び出し

            if(Objects.equals(rainbowCheck, "")){//もし空白なら
                print("どうやら見つからなかったようです。申し訳ありません。\n");
            } else {
                String[] rainbowtableCheckList = rainbowCheck.split("\t");//\t(空白)で区切る。Index0は平文、Index1はハッシュ方式
                print("見つかりました。 "+rainbowtableCheckList[0]+"です\n使用されている方式は、 "+rainbowtableCheckList[1]+" でした。");

                return;//ここで終了
            }
        }
        print("ハッシュ方式を指定してください(先頭の数字を入力してください)\n");

        String suiteiHashType = switch (CrackString.replace("00000000","").length()) {//MD5に付加される8つの0を削除して計算しています。(Javaで生成した時に発生する
            case 32 -> " MD5:1";//見方 ハッシュ方式:インデックス(1高いです)
            case 40 -> " SHA-1:2";
            case 56 -> " SHA-224:3 SHA3-224:7";
            case 64 -> " SHA-256:4, SHA3-256:8";
            case 96 -> " SHA-384:5 SHA3-384:9";
            case 128 -> " SHA-512:6, SHA3-512:10";
            default -> "見つかりませんでした。サポートしていないハッシュ値の可能性が高いです";
        };

        print("プログラムが推定したハッシュ方式:"+suiteiHashType+"\n");
        int S = 1;
        for(String hashtype : HashTypes){//For文で全出力
            print(S+":"+hashtype+"\n");
            S++;
        }
        print(">");

        HASHTYPE = HashTypes[Integer.parseInt(scanner.next()) - 1];//parseIntでintにしてから、差分である1を引いています
        if(HASHTYPE.equalsIgnoreCase("md5")) StringFormat = "%020x";//もしMD5なら、%020xを代入

        print("最大の原文パスワードの長さを指定してください:");
        endStringLength = Integer.parseInt(scanner.next());//終了する、平文パスワードの最大文字数(8の場合9を超えると終了)

        print("2の何倍のスレッド数を使用しますか？:");
        int times2 = Integer.parseInt(scanner.next());

        if(times2 < 1){//1未満ならば1を代入(そもそもスレッドなくなるから
            times2 = 1;
        }

        if(times2 > 16){//16より大きいならば16を代入(16以上だと、割り当てられる文字列がなくなるため
            times2 = 16;
        }

        int times22 = Chars.length() / (2 * times2);//2で掛け算した後にCharsの長さを割っている。startIndexとendIndexの算出に使用される
        print("Debug "+times22+" "+(times2 * 2)+"\n");

        //なんか眠いです
        for(int i=1;i<=(times2 * 2);i++) {
            int startIndex = (i-1) * times22;//0を掛け算すると0になる(?)
            int endIndex = i * times22;

            DecryptThread thread = new DecryptThread(endStringLength,CrackString,Chars2,startIndex,endIndex);//インスタンスを作成して値を引き渡している
            thread.start();
        }

        long startedMill = System.currentTimeMillis();//全スレッド開始終了時のミリ秒を記録

        String ProgressBar = "-";//くるくる回るあれです
        while(!gotPw) {//gotPwがfalseなら実行
            switch (ProgressBar) {//回す処理です
                case "-" -> ProgressBar = "\\";
                case "\\" -> ProgressBar = "|";
                case "|" -> ProgressBar = "/";
                case "/" -> ProgressBar = "-";
            }
            print("\rProcessing "+ProgressBar);

            Thread.sleep(100);//0.1秒待機
        }
        print("\nResult:"+OKPw.replace("\n","\\n"));//結果を表示

        long endedtime = System.currentTimeMillis() - startedMill;//何ミリ秒かかったか記録
        print("\nUp time "+(int) Math.ceil((endedtime / 1000) / 60)+" Minutes, "+(int) Math.ceil(endedtime / 1000)+" Seconds, "+(endedtime % 1000)+" Mill seconds\n");
        //endedtimeを1000で割って切り捨てすると秒数、1000で割って60で割って切り捨てすると分、1000で割ったあまりを計算するとミリ秒
    }

    /**
     * System.out.printをいちいち書くのが面倒だったので、短く書けるように作成したメソッドです。
     * @param string 出力する内容
     */
    private static void print(String string){
        System.out.print(string);
    }

    /**
     * Yesに関連する語句が入っているか？を検知する関数です
     * @param string 一致するかどうか検知する文字列
     * @return trueなら入っている、falseなら入っていない
     */
    private static boolean isYes(String string){
        return string.equals("yes") || string.equals("y") || string.equals("true");
    }

    /**
     * ハッシュ値を返すメソッド。MainクラスのStringFormat変数を使用しています
     * @param UsingHashInstance 使用するハッシュのMessageDigest型変数
     * @param content ハッシュにしたいString型の原文
     * @return ハッシュ化されたString型文字列
     */
    private static String Tohash(MessageDigest UsingHashInstance,String content){
        byte[] HashedByte = UsingHashInstance.digest(content.getBytes());//byte単位でハッシュ化
        return String.format(StringFormat,new BigInteger(1,HashedByte));//Stringに直す
    }

    /**
     * ディクショナリーアタックにより効率化を測ったものです。rainbowtableみたいな方式ではないのはJson扱いがとても面倒そうだったからです
     * @param hash 解析するハッシュ値
     * @return String型の平文パスワード+\t+ハッシュ値のタイプ。split("\t")を使用することにより扱いが可能。""が返ってくると存在しないという意味です
     * @throws NoSuchAlgorithmException 引き渡されたハッシュ関数名が存在しません。
     */
    private static String rainbowtableCheck(String hash) throws NoSuchAlgorithmException {
        List<String> hashtypeE = new ArrayList<>();//効率化のために、推測したハッシュ値をここに格納
        List<String> HASHTYPES = new ArrayList<>(List.of(HashTypes));//HashTypesを格納
        switch (CrackString.replace("0000","").length()) {//JavaでMD5を生成した時に発生する8つの0を削除してから長さを測っている
            case 32 -> {//推定されたハッシュ値をhashtypeEに追加して、HASYTPESから削除している
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
            default -> {//どれにも当てはまらない場合は全てためす
                for (String hashtype : HashTypes) {
                    for (String str : usefulpassword.split("\n")) {
                        MessageDigest MD = MessageDigest.getInstance(hashtype);
                        String hashed = Tohash(MD, str);
                        if(hash.equals(hashed)) return str+"\t"+hashtype;
                        hashed = Tohash(MD,str.toUpperCase());//大文字バージョンも試す
                        if(hash.equals(hashed)) return str+"\t"+hashtype;
                    }
                }
                return "";
            }
        }


        for(String hashtype : hashtypeE){//推定されたハッシュ値を試す
            for(String str : usefulpassword.split("\n")){
                MessageDigest MD = MessageDigest.getInstance(hashtype);
                String hashed = Tohash(MD,str);
                if(hash.equals(hashed)) return str+"\t"+hashtype;
                hashed = Tohash(MD,str.toUpperCase());//大文字バージョンも試す
                if(hash.equals(hashed)) return str+"\t"+hashtype;
            }
        }


        for(String hashtype : HASHTYPES){//上記で検知できなかった場合残りで試す
            for(String str : usefulpassword.split("\n")){
                MessageDigest MD = MessageDigest.getInstance(hashtype);
                String hashed = Tohash(MD, str);
                if(hash.equals(hashed)) return str+"\t"+hashtype;
                hashed = Tohash(MD,str.toUpperCase());//大文字バージョンも試す
                if(hash.equals(hashed)) return str+"\t"+hashtype;
            }
        }
        return "";
    }

    /**
     * rainbowtableCheck関数で使用するリストです。RockYouから拾ってきました
     */
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

    /**
     * 0x00~0xFFまで生成する関数です...
     * @return u0~255までのchar配列
     */
    private char[] ZeroTo255() {
        char[] result = new char[256];
        for (int i = 0; i < 256; i++) {
            result[i] = (char) i;
        }
        return result;
    }
}
