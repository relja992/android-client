package com.example.ljuba.trucks_client.model;

import java.util.Date;

/**
 * Created by VS-PC on 13.12.2017..
 */

public class PutniNalog {

    private String id_pn;
    private String broj_pn;
    private String vozac;
    private String status_pn;
    private String vreme_unosa;
    private String reg_oznaka_vozila;
    private String vrsta_vozila;

    public PutniNalog(){}

    public PutniNalog(String id_pn, String broj_pn, String vozac, String status_pn, String vreme_unosa, String reg_oznaka_vozila, String vrsta_vozila) {
        this.id_pn = id_pn;
        this.broj_pn = broj_pn;
        this.vozac = vozac;
        this.status_pn = status_pn;
        this.vreme_unosa = vreme_unosa;
        this.reg_oznaka_vozila = reg_oznaka_vozila;
        this.vrsta_vozila = vrsta_vozila;
    }

    public String getId_pn() {
        return id_pn;
    }

    public void setId_pn(String id_pn) {
        this.id_pn = id_pn;
    }

    public String getBroj_pn() {
        return broj_pn;
    }

    public void setBroj_pn(String broj_pn) {
        this.broj_pn = broj_pn;
    }

    public String getVozac() {
        return vozac;
    }

    public void setVozac(String vozac) {
        this.vozac = vozac;
    }

    public String getStatus_pn() {
        return status_pn;
    }

    public void setStatus_pn(String status_pn) {
        this.status_pn = status_pn;
    }

    public String getVreme_unosa() {
        return vreme_unosa;
    }

    public void setVreme_unosa(String vreme_unosa) {
        this.vreme_unosa = vreme_unosa;
    }

    public String getReg_oznaka_vozila() {
        return reg_oznaka_vozila;
    }

    public void setReg_oznaka_vozila(String reg_oznaka_vozila) {
        this.reg_oznaka_vozila = reg_oznaka_vozila;
    }

    public String getVrsta_vozila() {
        return vrsta_vozila;
    }

    public void setVrsta_vozila(String vrsta_vozila) {
        this.vrsta_vozila = vrsta_vozila;
    }
}
