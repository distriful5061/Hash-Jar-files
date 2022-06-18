import java.math.BigInteger;
import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 * 入力された平文からハッシュ値を生成するクラス
 * @author distriful5061
 * @version 1.0.0
 */
public class Main {
    /**
     * 使用するハッシュ値のリスト
     */
    private static final String[] HashTypes = {"MD2","MD5","SHA-1","SHA-224","SHA-256","SHA-384","SHA-512","SHA3-224","SHA3-256","SHA3-384","SHA3-512"};

    /**
     * メインのメソッド
     * @param args 実行時に渡された引数のリスト
     * @throws NoSuchAlgorithmException 指定されたハッシュ関数名が存在しません
     */
    public static void main(String[] args) throws NoSuchAlgorithmException {
        Scanner scanner = new Scanner(System.in);

        print("ハッシュ化する文字列を入力してください:");
        String hashString = scanner.next();

        for(String HASH : HashTypes) {
            MessageDigest UsingHashInstance = MessageDigest.getInstance(HASH);
            print(HASH+":"+Tohash(UsingHashInstance, hashString) + "\n");
        }
    }

    /**
     * System.out.printをいちいち書くのが面倒なので、短く書けるように作りました。
     * @param string
     */
    private static void print(String string){
        System.out.print(string);
    }

    /**
     * ハッシュ値を返すメソッド。
     * @param UsingHashInstance 使用するハッシュのMessageDigest型変数
     * @param content ハッシュにしたいString型の原文
     * @return ハッシュ化されたString型変数
     * @throws NoSuchAlgorithmException 指定されたハッシュ関数名が存在しません
     */
    private static String Tohash(MessageDigest UsingHashInstance,String content) throws NoSuchAlgorithmException {
        String StringFormat = "%040x";
        if(UsingHashInstance == MessageDigest.getInstance(HashTypes[0])) StringFormat = "%020x";

        byte[] HashedByte = UsingHashInstance.digest(content.getBytes());
        return String.format(StringFormat,new BigInteger(1,HashedByte));
    }
}