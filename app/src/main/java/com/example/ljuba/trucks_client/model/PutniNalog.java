package com.example.ljuba.trucks_client.model;

import java.util.Date;

/**
 * Created by VS-PC on 13.12.2017..
 */

public class PutniNalog {
    private String brojPN;
    private String vozac;
    private String vozilo;
    private String status;

    public PutniNalog(String brojPN, String vozac, String vozilo, String status) {
        this.brojPN = brojPN;
        this.vozac = vozac;
        this.vozilo = vozilo;
        this.status = status;
    }

    public PutniNalog() {
    }

    public String getBrojPN() {
        return brojPN;
    }

    public void setBrojPN(String brojPN) {
        this.brojPN = brojPN;
    }

    public String getVozac() {
        return vozac;
    }

    public void setVozac(String vozac) {
        this.vozac = vozac;
    }

    public String getVozilo() {
        return vozilo;
    }

    public void setVozilo(String vozilo) {
        this.vozilo = vozilo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
