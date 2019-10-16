package id.co.tornado.billiton.handler;

/**
 * Created by imome on 1/10/2019.
 */

public class SingleTagParser {
    String stag;
    String htag;
    int tlen;
    String hlen;
    String hval;
    int vlen;
    int cpflag;
    String rawData;
    String rawResult;

    public SingleTagParser(String stag, int cpflag, String rawInput) {
        this.stag = stag;
        this.cpflag = cpflag;
        this.rawData = rawInput;
        tlen = stag.length();
        htag = rawData.substring(0,tlen);
        hlen = rawData.substring(tlen,tlen+2);
        vlen = Integer.parseInt(hlen, 16) * 2; //template length
        if (vlen>256) {
            tlen+=2;
            hlen = rawData.substring(tlen,tlen+2);
            vlen = Integer.parseInt(hlen, 16) * 2; //template length
        }
        hval = rawData.substring(tlen+2,vlen+tlen+2);
        this.rawResult = rawData.substring(tlen+2+(vlen*cpflag));
    }

    public String getRawResult() {
        return rawResult;
    }

    public String getHval() {
        return hval;
    }
}
