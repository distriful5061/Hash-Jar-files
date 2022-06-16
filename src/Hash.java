import java.math.BigInteger;
import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Main {
    private static final String[] HashTypes = {"MD5","SHA-1","SHA-256","SHA-512","SHA3-256","SHA3-512"};

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Scanner scanner = new Scanner(System.in);

        print("ハッシュ化する文字列を入力してください:");
        String hashString = scanner.next();

        for(String HASH : HashTypes) {
            MessageDigest UsingHashInstance = MessageDigest.getInstance(HASH);
            print(HASH+":"+Tohash(UsingHashInstance, hashString) + "\n");
        }
    }

    private static void print(String string){
        System.out.print(string);
    }

    private static String Tohash(MessageDigest UsingHashInstance,String content) throws NoSuchAlgorithmException {
        String StringFormat = "%040x";
        if(UsingHashInstance == MessageDigest.getInstance(HashTypes[0])) StringFormat = "%020x";

        byte[] HashedByte = UsingHashInstance.digest(content.getBytes());
        return String.format(StringFormat,new BigInteger(1,HashedByte));
    }
}