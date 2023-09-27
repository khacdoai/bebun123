package model;

public class ThongtinDuLieu {
    private String TieuDe;
    private String Date;
    private String nguoigui;

    public ThongtinDuLieu(String TieuDe, String Date, String nguoigui) {
        this.TieuDe = TieuDe;
        this.Date = Date;
        this.nguoigui = nguoigui;
    }

    public String getTieuDe() {
        return TieuDe;
    }

    public void setTieuDe(String TieuDe) {
        this.TieuDe = TieuDe;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String Date) {
        this.Date = Date;
    }

    public String getNguoigui() {
        return nguoigui;
    }

    public void setNguoigui(String nguoigui) {
        this.nguoigui = nguoigui;
    }
    
}
