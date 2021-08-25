// *****************************************************************************
// * MainActivity.java: calculator instances creation and deletion, interfaces handling
// *****************************************************************************
// * Copyright (C) 2021 Vincent Chou
// *
// * Authors: Vincent Chou <vchou79@gmail.com>
// *
// * This program is free software; you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation; either version 2.1 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with this program; if not, write to the Free Software Foundation,
// * Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
// *****************************************************************************

package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    String lastButton, lastNum;
    double lastRes;
    ArrayList<String> ar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ar=new ArrayList<String>();
    }

//    https://docs.oracle.com/javase/6/docs/api/java/lang/Double.html#valueOf%28java.lang.String%29
    public double valueOfADouble(String i) {
        final String Digits     = "(\\p{Digit}+)";
        final String HexDigits  = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp        = "[eE][+-]?"+Digits;
        final String fpRegex    =
                ("[\\x00-\\x20]*"+  // Optional leading "whitespace"
                        "[+-]?(" + // Optional sign character
                        "NaN|" +           // "NaN" string
                        "Infinity|" +      // "Infinity" string

                        // A decimal floating-point string representing a finite positive
                        // number without a leading sign has at most five basic pieces:
                        // Digits . Digits ExponentPart FloatTypeSuffix
                        //
                        // Since this method allows integer-only strings as input
                        // in addition to strings of floating-point literals, the
                        // two sub-patterns below are simplifications of the grammar
                        // productions from the Java Language Specification, 2nd
                        // edition, section 3.10.2.

                        // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                        "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

                        // . Digits ExponentPart_opt FloatTypeSuffix_opt
                        "(\\.("+Digits+")("+Exp+")?)|"+

                        // Hexadecimal strings
                        "((" +
                        // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "(\\.)?)|" +

                        // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                        ")[pP][+-]?" + Digits + "))" +
                        "[fFdD]?))" +
                        "[\\x00-\\x20]*");// Optional trailing "whitespace"

        if (Pattern.matches(fpRegex, i))
            return Double.valueOf(i); // Will not throw NumberFormatException
        else {
            return 0.0;
            // Perform suitable alternative action
        }
    }

    public boolean isADouble(String i) {
        if(!String.valueOf(valueOfADouble(i)).equals("0.0")) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isDivision(String i) {
        if(i.equals("/")) {
            return true;
        }
        return false;
    }

    public ArrayList<String> division(ArrayList<String> s) {
        ArrayList<String> res=new ArrayList<String>();
        double d=0.0;
        if(s.size()==1) {
            res=s;
        }
        for(int i=0; s.size()>=3 && i<s.size()-1; i+=2) {
            if(isDivision(s.get(i+1))) {
                if(i!=s.size()-1 && isADouble(s.get(i)) && isADouble(s.get(i+2))) {
                    d=calc(s.get(i+1), valueOfADouble(s.get(i)), valueOfADouble(s.get(i+2)));
                    res.add(String.valueOf(d));
                    if(i+3<s.size() && !isDivision(s.get(i+3)) && isAMode(s.get(i+3))) {
                        res.add(s.get(i+3));
                        i+=2;
                        if(i+2==s.size()-1) {
                            res.add(s.get(i+2));
                        }
                    }
                    else if(i+4<s.size() && isDivision(s.get(i+3))) {
                        d=calc(s.get(i+3), d, valueOfADouble(s.get(i+4)));
                        res.remove(res.size()-1);
                        res.add(String.valueOf(d));
                        i+=4;
                        if(i+1<s.size()) {
                            res.add(s.get(i+1));
                            if(i+2==s.size()-1 && isAMode(s.get(i+1)) && isADouble(s.get(i+2))) {
                                res.add(s.get(i+2));
                            }
                        }
                    }
                }
            }
            else if(!isDivision(s.get(i+1))) {
                res.add(s.get(i));
                res.add(s.get(i+1));
                if(i+2==s.size()-1) {
                    res.add(s.get(i+2));
                }
            }
            if((res.size()>1 && isAMode(res.get(res.size()-1)) || res.size()==0)
                    && s.size()-2>0 && i==s.size()-1) {
                if(isAMode(s.get(s.size()-2)) && !isDivision((s.get(s.size()-2)))) {
                    res.add(s.get(s.size()-1));
                }
            }
        }
        return res;
    }

    public static String removeLastChar(String s) {
        return (s == null || s.length() == 0)
                ? null
                : (s.substring(0, s.length() - 1));
    }

    public void showInfo(View v) {
        Button b=(Button) findViewById(v.getId());
        TextView tv=(TextView) findViewById(R.id.textView);
        double iNum=0.0;
        double iiNum=0.0;
        String opMode="";
        ArrayList<String> commonAr;
        commonAr=new ArrayList<String>();

        if(lastNum==null) {
            lastNum="";
        }

        if(isADouble(lastNum)) {
            if(!isDecimalNul(valueOfADouble(lastNum)) && b.getText().equals(".")) {
                return;
            }
        }
        else if(b.getText().equals(".")) {
            if(lastNum.contains(".")) {
                return;
            }
        }

        if(tv.getText().equals("0") && !isAMode(b.getText().toString())) {
            tv.setText(b.getText().toString());
            lastNum=b.getText().toString();
            lastButton=b.getText().toString();
        }
        else if(b.getText().equals("C")) {
            tv.setText("0");
            lastRes=0.0;
            lastButton="";
            lastNum="";
            ar.clear();
        }
        else if(b.getText().equals("D")) {
            if(!lastNum.equals("")) {
                ar.add(lastNum);
                lastNum="";
            }

            if(!tv.getText().equals("0")) {
                if(isAMode(lastButton)) {
                    lastButton="";
                }
                String backspace=removeLastChar(tv.getText().toString());
                if(!backspace.equals("")) {
                    tv.setText(backspace);
                }
                else {
                    tv.setText("0");
                }
                backspace="";
                if(ar.size()>0) {
                    backspace=removeLastChar(ar.get(ar.size()-1).toString());
                    if(backspace.length()==0) {
                        ar.remove(ar.size()-1);
                    }
                    if(backspace.length()>0) {
                        ar.add(backspace);
                    }
                }
            }
        }
        else if(lastButton!=null && isAMode(b.getText().toString()) && isAMode(lastButton))
            System.out.println("Error: last press was also a mode");
        else if(!tv.getText().equals("0") && !b.getText().equals("=")) {
            if(isAMode(b.getText().toString())) {
                if(ar.size()<=0) {
                    lastNum=tv.getText().toString();
                }
                lastButton=b.getText().toString();
                if(lastNum.length()>0) {
                    ar.add(lastNum);
                }
                lastNum="";
                ar.add(b.getText().toString());
            }
            else {
                lastNum+=b.getText().toString();
                lastButton=b.getText().toString();

                if(!lastButton.equals("")) {
                    lastButton="";
                }
            }
            tv.setText(tv.getText()+b.getText().toString());
        }
        else if(b.getText().equals("=") && ar.size()!=0) {
            boolean isPrioritised=false;
            int i_lastRes=0;
            int i_iiNum=0;
            int i_iNum=0;

            if(lastNum.length()>0) {
                ar.add(lastNum);
                lastNum="";
            }

            ar=division(ar);
            if(ar.size()==1) {
                commonAr=ar;
            }
            for(int i=0; ar.size()>1 && i<ar.size(); i+=3) {
                if (i == 0) {
                    iNum = valueOfADouble(ar.get(i));
                }
                else if(i<ar.size() && isAMode(ar.get(i))) {
                    opMode=ar.get(i);
                    if(opMode.equals("*")) {
                        if(i-2>=0 && isAMode(ar.get(i-2)) && !ar.get(i-2).equals("*")) {
                            iNum=valueOfADouble(ar.get(i-1));
                        } else if(i-2>=0 && isAMode(ar.get(i-2)) && ar.get(i-2).equals("*")) {
                            iNum=lastRes;
                            if(commonAr.size()>0 && String.valueOf(lastRes)==commonAr.get(commonAr.size()-1)) {
                                commonAr.remove(commonAr.size()-1);
                                if(i<ar.size()-1 && isAMode(ar.get(i)) && ar.get(i).equals("*")) {
                                    lastRes=iNum*valueOfADouble(ar.get(i+1));
                                    commonAr.add(String.valueOf(lastRes));
                                }
                            }
                        }
                        isPrioritised=true;
                    } else {
                        isPrioritised=false;
                    }
                }
                else if(i<ar.size() && !isAMode(ar.get(i))) {
                    if(i<ar.size() && ar.get(i-1).equals("*")) {
                        iNum=lastRes*valueOfADouble(ar.get(i));
                        i_iNum=i;
                        if(commonAr.size()!=0 && commonAr.get(commonAr.size()-1)==String.valueOf(lastRes)) {
                            if(i<ar.size()-1 && i-3>=0 && isAMode(ar.get(i-3)) && ar.get(i-3).equals("*")) {
                                commonAr.remove(commonAr.size() - 1);
                            }
                        }
                        if(i+1>=ar.size()) {
                            if(commonAr.size()-1>=0 && !isAMode(commonAr.get(commonAr.size()-1))) {
                                if(i==ar.size()-1 && i_iNum!=i_iiNum) {
                                    commonAr.remove(commonAr.size()-1);
                                }
                                commonAr.add(String.valueOf(iNum));
                            }
                        } else if(commonAr.size()!=0 && !isAMode(commonAr.get(commonAr.size()-1))) {
                            if(i<ar.size()-1 && i-3>=0 && isAMode(ar.get(i-3)) && ar.get(i-3).equals("*")) {
                                commonAr.remove(commonAr.size()-1);
                                commonAr.add(String.valueOf(iNum));
                            }
                        } else if(commonAr.size()!=0 && isAMode(commonAr.get(commonAr.size()-1))) {
                            commonAr.add(String.valueOf(iNum));
                        }
                    } else if(i<ar.size() && isAMode(ar.get(i-1)) && !ar.get(i-1).equals("*")) {
                        if(commonAr.size()==0) {
                            commonAr.add(String.valueOf(lastRes));
                            commonAr.add(ar.get(i-1));
                        }
                        else {
                            commonAr.add(ar.get(i-1));
                            if(i+1>=ar.size()) {
                                commonAr.add(ar.get(i));
                            }
                        }
                    }
                }
                else {
                    iNum = lastRes;
                }
                if (i<ar.size()-1 && isAMode(ar.get(i+1))) {
                    opMode=ar.get(i+1);
                    if (opMode.equals("*")) {
                        isPrioritised = true;
                    } else isPrioritised = false;
                } else if (i<ar.size()-1 && !isAMode(ar.get(i+1))) {
                    if (commonAr.size()>0 && isAMode(commonAr.get(commonAr.size()-1))) {
                        if(i<ar.size()-2 && isAMode(ar.get(i+2))) {
                            opMode=ar.get(i+2);
                            if(opMode.equals("*")) {
                                if (i<ar.size()-3 && !isAMode(ar.get(i+3))) {
                                    iiNum=valueOfADouble(ar.get(i+3));
                                    i_iiNum=i+3;
                                }
                                isPrioritised=true;
                            } else isPrioritised=false;
                        }
                        if(isAMode(ar.get(i)) && ar.get(i).equals("*")) {
                            opMode=ar.get(i);
                            isPrioritised=true;
                            iiNum = valueOfADouble(ar.get(i+1));
                            i_iiNum=i+1;
                        } else {
                            iNum = valueOfADouble(ar.get(i+1));
                            i_iNum=i+1;
                        }
                        if(!isPrioritised) {
                            commonAr.add(ar.get(i+1));
                        } else {
                            lastRes=calc(opMode, iNum, iiNum);
                            i_lastRes=i;
                            if(i>=ar.size()-2) {
                                commonAr.add(String.valueOf(lastRes));
                            }
                            else if(isAMode(commonAr.get(commonAr.size()-1))) {
                                commonAr.add(String.valueOf(lastRes));
                            }
                        }
                    }
                    else if(isPrioritised) {
                        iiNum=valueOfADouble(ar.get(i+1));
                        lastRes=calc(opMode, iNum, iiNum);
                        if(i<ar.size()-1 && i-2>=0 && isAMode(ar.get(i-2)) && ar.get(i-2).equals("*")) {
                            commonAr.remove(commonAr.size()-1);
                            commonAr.add(String.valueOf(lastRes));
                        }
                    }
                }
                if(i<ar.size()-2 && !isAMode(ar.get(i+2))) {
                    iiNum=valueOfADouble(ar.get(i+2));
                    if(isPrioritised) {
                        if(i>0 && i<ar.size() && isAMode(ar.get(i-1)) && !ar.get(i-1).equals("*")) {
                            iNum=valueOfADouble(ar.get(i));
                            i_iNum=i;
                        }
                        if(i_iNum!=i_iiNum || (i_iNum==0 && i_iiNum==0 && i_lastRes==0 && lastRes==0 && commonAr.size()==0)) {
                            lastRes=calc(opMode, iNum, iiNum);
                            i_lastRes=i;
                        } else {
                            lastRes=calc(opMode, lastRes, iiNum);
                            i_lastRes=i;
                        }
                        if(commonAr.size()-1>0 && !isAMode(commonAr.get(commonAr.size()-1))) {
                            commonAr.remove(commonAr.size()-1);
                        }
                        if(i_lastRes==0 && lastRes!=0 && commonAr.size()==0) {
                            commonAr.add(String.valueOf(lastRes));
                        }
                        else {
                            if(commonAr.size()==1 && !isAMode(commonAr.get(commonAr.size()-1))) {
                                commonAr.remove(commonAr.size()-1);
                            }
                            commonAr.add(String.valueOf(lastRes));
                        }
                        if(i<ar.size()-3 && isAMode(ar.get(i+3))) {
                            if(!ar.get(i+3).equals("*")) {
                                commonAr.add(ar.get(i+3));
                            }
                        }
                    } else {
                        if(i>0 && i<ar.size() && isAMode(ar.get(i-1)) && !ar.get(i-1).equals("*")) {
                            commonAr.add(ar.get(i));
                        }
                        else if(i==0) {
                            commonAr.add(ar.get(i));
                        }
                        commonAr.add(opMode);
                        if(i>=ar.size()-3) {
                            commonAr.add(String.valueOf(iiNum));
                        }
                    }
                }
                if(i<ar.size()-3 && isAMode(ar.get(i+3)) && !isPrioritised) {
                    if(!ar.get(i+3).equals("*")) {
                        commonAr.add(ar.get(i+2));
                        commonAr.add(ar.get(i+3));
                    }
                }
            }
            for(int i=0; commonAr.size()>1 && i<commonAr.size(); i+=2) {
                double a, c;
                if(i==0) {
                    a=valueOfADouble(commonAr.get(i));
                    if(i==commonAr.size()-1) {
                        lastRes=valueOfADouble(commonAr.get(i));
                    }
                }
                else {
                    a=lastRes;
                }
                if(i<commonAr.size()-1) {
                    opMode=commonAr.get(i+1);
                }
                if(i<commonAr.size()-2) {
                    c=valueOfADouble(commonAr.get(i+2));
                    lastRes=calc(opMode, a, c);
                }
            }
            if(commonAr.size()==1) {
                lastRes=valueOfADouble(commonAr.get(0));
            }
            if(commonAr.size()==0) {
                tv.setText("Math ERROR");
            }
            else if(!isDecimalNul(lastRes)) {
                tv.setText(String.format("%.11f", lastRes));
            }
            else {
                tv.setText(String.format("%.0f", lastRes));
            }

            lastRes=0.0;
            lastButton="";
            lastNum="";
            ar.clear();
            commonAr.clear();
        }
    }

    public boolean isDecimalNul(double i) {
        BigDecimal bigDecimal=new BigDecimal(String.valueOf(i));
        int intValue=bigDecimal.intValue();
        String s=bigDecimal.subtract(new BigDecimal(intValue)).toPlainString();
        double d=valueOfADouble(s);
        if(d>0.0) {
            return false;
        }
        else {
            return true;
        }
    }

    public double calc(String o, double i, double ii) {
        switch (o) {
            case "/":
                return i / ii;
            case "+":
                return i + ii;
            case "-":
                return i - ii;
            case "*":
                return i * ii;
            default:
                System.out.println("Unknown error");
                return 0.0;
        }
    }

    public Boolean isAMode(String s) {
        Boolean res=false;

        if(s.equals("C") || s.equals("=") || s.equals("D")
                || s.equals("-") || s.equals("+") || s.equals("*") || s.equals("/")) {
            res=true;
        }
        return res;
    }
}