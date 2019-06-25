package util;

import java.util.*;

/**
 * Created by oliver on 2018/1/8.
 */
public class Matrix{
    private List<List<Integer>> matrix;
    private int m;
    private int n;

    public int getM(){
        return m;
    }

    public int getN(){
        return n;
    }

    public Matrix(int m , int n){
        this.m = m;
        this.n = n;
        matrix = new ArrayList<>();
        //深度拷贝
        try {
            for(int i = 0 ; i < m ; i ++) {
                List<Integer> row = new ArrayList<Integer>();
                for (int j = 0; j < n; j++){
                    row.add(0);
                }
                matrix .add(row);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getValue(int i , int j){
        if(i < m && j < n)
            return matrix.get(i).get(j);
        else
            return -1;
    }

    public void setValue(int i , int j , int value){
        matrix.get(i).set(j, value);
    }

    public void print(int length){
        Formatter formatter = new Formatter();
        formatter.format("%-" + (length + 2)+ "s" , " ");

        for(int j = 0 ; j < n ; j ++){
            formatter.format("%-" + (length + 2) + "s " , j + 1 + "");
        }
        formatter.format("\n");

        for(int i = 0 ; i < m ; i ++){
            formatter.format("%-" + (length + 2) + "s" , i + 1 + "");
            for(int j = 0 ; j < n ; j ++){
                int value = matrix.get(i).get(j);
                formatter.format("%."+ length + "f " , (double)value);
            }
            formatter.format("\n");
        }
        System.out.print(formatter);

    }

    public void print(double min){
        Formatter formatter = new Formatter(new StringBuilder( ), Locale.US);
        formatter.format("%-3s" , " ");

        for(int j = 0 ; j < n ; j ++){
            formatter.format("%-3s " , j + "");
        }
        formatter.format("\n");

        for(int i = 0 ; i < m ; i ++){
            formatter.format("%-3s" , i + "");
            for(int j = 0 ; j < n ; j ++){
                int value = matrix.get(i).get(j);
                if(value > min){
                    formatter.format("%.1f " , (double)value);
                }else
                    formatter.format("    ");
            }
            formatter.format("\n");
        }

        System.out.print(formatter);
    }
}
