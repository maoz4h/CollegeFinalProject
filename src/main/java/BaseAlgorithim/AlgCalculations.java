package BaseAlgorithim;
import java.util.Vector;
import matrix.*;
import DB.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author асу
 */
public class AlgCalculations {
    private Matrix compareDataMatrix = null;
    private Matrix scoreVector = null;
    private Matrix formulaVector = null;
    
    public Matrix getCompareDataMatrix() {
        if(compareDataMatrix == null){
            createCompareDataInMatrix();
        }
        return compareDataMatrix;
    }

    public Matrix getScoreVector() {
        if(scoreVector == null){
            createScoreVector();
        }
        return scoreVector;
    }

    public Vector<Double> getFormulaVector() {
        Vector<Double> formula = new Vector<>();
        if(formulaVector == null){
            if(DBControl.isFormulaExists()){
                formula = DBControl.getFormula();
                formulaVector = VectorToMatrix(formula);
            }
            else{
                createFormulaVector();
                Vector<Double> fvec = MatrixToVector(formulaVector);
                DBControl.insertFormula(fvec);
            }
        }
        return MatrixToVector(formulaVector);
    }

    static public Vector<Double> createCompareVector(Vector<Double> A,Vector<Double> B){
         int i,j;
         Vector<Double> toReturn = new Vector<Double>();
         for(i=0,j=0; i<A.size() && j<B.size(); i++,j++){
             toReturn.add(Math.pow(Math.abs(A.get(i)-B.get(j)),2));
         }
         return toReturn;
         
    }
    private void createCompareDataInMatrix(){
        
        compareDataMatrix = DBControl.getCompareAlgMatrix();
    }
    private void createScoreVector(){
        Vector<Double> vec = DBControl.getScoreVector();
        scoreVector = VectorToMatrix(vec);
    }
    private void createFormulaVector(){
        Matrix A = getCompareDataMatrix();
        Matrix c = getScoreVector();
        Matrix Atrans = MatrixMathematics.transpose(A);
        Matrix AtMultA = MatrixMathematics.multiply(Atrans, A);
        Matrix result = null;
        /*try {
            result = MatrixMathematics.inverse(AtMultA);
        } catch (NoSquareException ex) {
            Logger.getLogger(AlgCalculations.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        double [][] res = MatrixMathematics.invert(AtMultA.getValues());
        result = new Matrix(res);
        result = MatrixMathematics.multiply(result, Atrans);
        result = MatrixMathematics.multiply(result, c);
        formulaVector = result;
    }
    private Matrix VectorToMatrix(Vector<Double> vec){
        Matrix mat = new Matrix(vec.size(),1);
        double[][] vals = new double[vec.size()][1];
        for(int i=0; i<vec.size(); i++){
            vals[i][0]=vec.get(i);
        }
        mat.setValues(vals);
        return mat;
    }
    
    private Vector<Double> MatrixToVector(Matrix mat){
        double[][] vec = mat.getValues();
        Vector<Double> toReturn = new Vector<>();
        for(int i=0; i<mat.getNrows(); i++){
            toReturn.add(vec[i][0]);
        }
        return toReturn;
    }
}
