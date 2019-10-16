package id.co.tornado.billiton.common;

/**
 * Created by imome on 1/11/2019.
 */

public class NsiccsData {

    private String aip; //card init
    private String tvr; //terminal init
    private String currency; //terminal init
    private String panseq;  //card init
    private String txdate; //terminal upd
    private String txtype;  //terminal init
    private String amount;  //terminal upd
    private String addamount;  //terminal upd
    private String iad;  //card init
    private String country;  //terminal init
    private String arqc;  //card upd
    private String atc; //card upd
    private String termRandomNum;  //terminal upd
    private String cardRandomNum;  //card upd
    private String arpc;  //host end
    private String arc;  //host end
    private String pan;  //card init
    private String track2;  //card init
    private String cdol; // read record
    private String cdol2; // read record
    private String aid;  // terminal init
    private String tc;  // tx validation
    private int txst; // tx validation
    private String tdol; // terminal init

    public NsiccsData() {
    }

    public String getAip() {
        return aip;
    }

    public void setAip(String aip) {
        this.aip = aip;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPanseq() {
        return panseq;
    }

    public void setPanseq(String panseq) {
        this.panseq = panseq;
    }

    public String getTxdate() {
        return txdate;
    }

    public void setTxdate(String txdate) {
        this.txdate = txdate;
    }

    public String getTxtype() {
        return txtype;
    }

    public void setTxtype(String txtype) {
        this.txtype = txtype;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAddamount() {
        return addamount;
    }

    public void setAddamount(String addamount) {
        this.addamount = addamount;
    }

    public String getIad() {
        return iad;
    }

    public void setIad(String iad) {
        this.iad = iad;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getArqc() {
        return arqc;
    }

    public void setArqc(String arqc) {
        this.arqc = arqc;
    }

    public String getAtc() {
        return atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    public String getTermRandomNum() {
        return termRandomNum;
    }

    public void setTermRandomNum(String termRandomNum) {
        this.termRandomNum = termRandomNum;
    }

    public String getCardRandomNum() {
        return cardRandomNum;
    }

    public void setCardRandomNum(String cardRandomNum) {
        this.cardRandomNum = cardRandomNum;
    }

    public String getArpc() {
        return arpc;
    }

    public void setArpc(String arpc) {
        this.arpc = arpc;
    }

    public String getArc() {
        return arc;
    }

    public void setArc(String arc) {
        this.arc = arc;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getTrack2() {
        return track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getCdol() {
        return cdol;
    }

    public void setCdol(String cdol) {
        this.cdol = cdol;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getTc() {
        return tc;
    }

    public void setTc(String tc) {
        this.tc = tc;
    }

    public String getCdol2() {
        return cdol2;
    }

    public void setCdol2(String cdol2) {
        this.cdol2 = cdol2;
    }


    public int getTxst() {
        return txst;
    }

    public void setTxst(int txst) {
        this.txst = txst;
    }

    public String getTdol() {
        return tdol;
    }

    public void setTdol(String tdol) {
        this.tdol = tdol;
    }

    @Override
    public String toString() {
        return getPan()+" "+getArqc()+" "+getTc()+" "+getAid();
    }
}
