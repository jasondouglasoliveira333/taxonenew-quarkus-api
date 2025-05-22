package br.com.lkm.taxone.mapper.dto;

public class ValueWrapper{
    int iValue;
    
    public Integer getInt(){
        return iValue;
    }
    
    public void addInt(int value){
        iValue += value;
    }

    public void addStringInt(String value){
        iValue += Integer.parseInt(value);
    }

    public int multiplyInt(int value){
        iValue += value;
        return iValue;
    }
    
}
