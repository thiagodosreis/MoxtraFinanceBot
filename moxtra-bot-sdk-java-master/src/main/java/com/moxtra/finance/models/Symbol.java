package com.moxtra.finance.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Symbol {
	private String id;
	private String t;
	private String e;
	private String l;
	private String l_fix;
	private String l_cur;
	private String s;
	private String ltt;
	private String lt;
	private String lt_dts;
	private String c;
	private String c_fix;
	private String cp;
	private String cp_fix;
	private String ccol;
	private String pcls_fix;
	private String el;
	private String el_fix;
	private String el_cur;
	private String elt;
	private String ec;
	private String ec_fix;
	private String ecp;
	private String ecp_fix;
	private String eccol;
	private String div;
	private String yld;
	
	public Symbol(){}
	
	public String getId(){
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getT() {
		return t;
	}

	public void setT(String t) {
		this.t = t;
	}

	public String getE() {
		return e;
	}

	public void setE(String e) {
		this.e = e;
	}

	public String getL() {
		return l;
	}

	public void setL(String l) {
		this.l = l;
	}

	public String getL_fix() {
		return l_fix;
	}

	public void setL_fix(String l_fix) {
		this.l_fix = l_fix;
	}

	public String getL_cur() {
		return l_cur;
	}

	public void setL_cur(String l_cur) {
		this.l_cur = l_cur;
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public String getLtt() {
		return ltt;
	}

	public void setLtt(String ltt) {
		this.ltt = ltt;
	}

	public String getLt() {
		return lt;
	}

	public void setLt(String lt) {
		this.lt = lt;
	}

	public String getLt_dts() {
		return lt_dts;
	}

	public void setLt_dts(String lt_dts) {
		this.lt_dts = lt_dts;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public String getC_fix() {
		return c_fix;
	}

	public void setC_fix(String c_fix) {
		this.c_fix = c_fix;
	}

	public String getCp() {
		return cp;
	}

	public void setCp(String cp) {
		this.cp = cp;
	}

	public String getCp_fix() {
		return cp_fix;
	}

	public void setCp_fix(String cp_fix) {
		this.cp_fix = cp_fix;
	}

	public String getCcol() {
		return ccol;
	}

	public void setCcol(String ccol) {
		this.ccol = ccol;
	}

	public String getPcls_fix() {
		return pcls_fix;
	}

	public void setPcls_fix(String pcls_fix) {
		this.pcls_fix = pcls_fix;
	}

	public String getEl() {
		return el;
	}

	public void setEl(String el) {
		this.el = el;
	}

	public String getEl_fix() {
		return el_fix;
	}

	public void setEl_fix(String el_fix) {
		this.el_fix = el_fix;
	}

	public String getEl_cur() {
		return el_cur;
	}

	public void setEl_cur(String el_cur) {
		this.el_cur = el_cur;
	}

	public String getElt() {
		return elt;
	}

	public void setElt(String elt) {
		this.elt = elt;
	}

	public String getEc() {
		return ec;
	}

	public void setEc(String ec) {
		this.ec = ec;
	}

	public String getEc_fix() {
		return ec_fix;
	}

	public void setEc_fix(String ec_fix) {
		this.ec_fix = ec_fix;
	}

	public String getEcp() {
		return ecp;
	}

	public void setEcp(String ecp) {
		this.ecp = ecp;
	}

	public String getEcp_fix() {
		return ecp_fix;
	}

	public void setEcp_fix(String ecp_fix) {
		this.ecp_fix = ecp_fix;
	}

	public String getEccol() {
		return eccol;
	}

	public void setEccol(String eccol) {
		this.eccol = eccol;
	}

	public String getDiv() {
		return div;
	}

	public void setDiv(String div) {
		this.div = div;
	}

	public String getYld() {
		return yld;
	}

	public void setYld(String yld) {
		this.yld = yld;
	}
	
	@Override
    public String toString() {
        return "Symbol{" +
                "id='" + id + '\'' +
                ", t='" + t + '\'' +
                ", l_fix='" + l_fix + '\'' +
                '}';
    }
}



