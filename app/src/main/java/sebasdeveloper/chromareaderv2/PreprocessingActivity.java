package sebasdeveloper.chromareaderv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Size;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.random;
import static org.opencv.core.Core.CMP_GT;
import static org.opencv.core.Core.FILLED;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_32S;
import static org.opencv.core.CvType.CV_64F;

public class PreprocessingActivity extends AppCompatActivity {
    Intent intent;
    //Bindeo para el imageview
    @BindView(R.id.image2) ImageView img2;
    @BindView(R.id.textView) TextView txt;
    Bitmap bmp;
    Mat ima;
    Mat imasinfondo;
    Mat capa2;
    Mat capa3;
    Mat capa1;
    int cont=1;
    //Clase donde se crea el layout y se inicializa la libreria ButterKnife
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preprocessing);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String nombre = intent.getStringExtra(CoreActivity.Nombre);
        String lugar = intent.getStringExtra(CoreActivity.Lugar);
        String descripcion = intent.getStringExtra(CoreActivity.Descripcion);
        txt.setText("Nombre:"+nombre+"\n"+"Lugar:"+lugar+"\n"+"Descripción:"+descripcion);
        procesarcroma();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.preprocessing,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.diagnostico) {
            intent = new Intent(this, MineralActivity.class);
            startActivity(intent);
        }
        if (id == R.id.procesamiento) {
            intent = new Intent(this, OrganicActivity.class);
            startActivity(intent);
        }
        if (id == R.id.derechos) {

        }
        return super.onOptionsItemSelected(item);
    }
    public void procesarcroma() {
        ima=imread_mat("cromaoriginal");
        int threshb1=45;
        int threshb2=95;
        int threshr1=160;
        int threshr2=110;
        imasinfondo=deletebackground();
        capa1=Mat.zeros(imasinfondo.size(),0);
        capa2=Mat.zeros(imasinfondo.size(),0);
        capa3=Mat.zeros(imasinfondo.size(),0);
        imwrite_mat(imasinfondo,"cromasinfondo");
        int rows=imasinfondo.rows();
        int cols=imasinfondo.cols();
        double[] capab={255};
        double[] capan={0};
        Mat compb=componente(0);
        Mat compr=componente(2);
        for (int i=0; i<rows; i++)
        {
            for (int j=0; j<cols; j++)
            {
                double[] pixb = compb.get(i, j);
                double[] pixr = compr.get(i, j);
                if (pixb[0]>threshb1 && pixb[0]<threshb2  ) {
                    capa2.put(i, j, capab);
                }
                else
                    capa2.put(i, j, capan);
                if (pixr[0]>threshr1 ) {
                    capa3.put(i, j, capab);
                }
                else
                    capa3.put(i, j, capan);
                if (pixr[0]>threshr2 && pixr[0]<threshr1  ) {
                    capa1.put(i, j, capab);
                }
                else
                    capa1.put(i, j, capan);
            }

        }

        capa2=segcapa2(capa2);
        imwrite_mat(capa2,"capa2");
        capa3=segcapa3(capa3);
        imwrite_mat(capa3,"capa3");
        capa1=segcapa1(capa1);
        imwrite_mat(capa1,"capa1");
        showima("cromasinfondo");
    }
    public Mat deletebackground(){;
        Mat temp = ima;
        int rows=ima.rows();
        int cols=ima.cols();
        int ch = ima.channels();
        double[] datocolor={0,0,0};
        {
            for (int i=0; i<rows; i++)
            {
                for (int j=0; j<cols; j++)
                {
                    double[] pix = ima.get(i, j);
                    if (abs(pix[0] -  pix[1])  < 10 ) {
                        temp.put(i, j, datocolor);}
                }
            }
        }

        return temp;
    }
    public Mat segcapa2(Mat temp)
    {
      //  Mat temp=componente(0);
       // temp=threshing(temp,45,95);
        temp=fillholes(temp,30000);
        return temp;
    }
    public Mat segcapa3(Mat temp)
    {   //Mat temp=componente(2);
        //temp=threshing(temp,160,256);
        temp=fillholes(temp,80000);
        return temp;
    }

    public Mat segcapa1(Mat temp)
    {
    int rows=imasinfondo.rows();
    int cols=imasinfondo.cols();
    //temp=componente(2);
    //temp=threshing(temp,110,160);
    double[] capab={255};
    double[] capan={0};
        for (int i=0; i<rows; i++)
    {
        for (int j=0; j<cols; j++)
        {
            double[] pix = capa2.get(i, j);
            if (pix[0]>0 ) {
                temp.put(i, j, capan);
            }
        }
    }
    temp=fillholes(temp,23000);
        return temp;
}

    public Mat imread_mat(String a){
        Mat imagen;
        String nombre=a+".jpg";
        //Se lee la foto desde la ubicacion donde fue almacenada en la memoria interna
        imagen = Imgcodecs.imread(Environment.getExternalStorageDirectory()+
                "/sebas/"+nombre);
        return imagen;
    }
    public void imwrite_mat(Mat imagen,String a){
       String nombre=a+".jpg";
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory()+
                "/sebas/"+nombre,imagen);
    }
    public void showima(String a){
        String nombre=a+".jpg";
        bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+
                "/sebas/"+nombre);
        img2.setImageBitmap(bmp);
    }

    public Mat componente(int c){
        Mat imagen=ima;
        List<Mat> canales = new ArrayList<Mat>();
        imagen.zeros(ima.size(),ima.type());
        Core.split(imasinfondo,canales);
        imagen=canales.get(c);
        return imagen;
    }
    public Mat fillholes(Mat tempp,int areas){
        tempp.zeros(ima.size(),ima.type());
        double areatotal=0;
        Point a= new Point(0,0);
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(tempp,contours,hierarchy,Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_NONE);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Mat area=contours.get(contourIdx);
            double area2=Imgproc.contourArea(area);
            if (area2<areas) {
                Imgproc.drawContours(tempp, contours,contourIdx, new Scalar(0, 0,0),-1,8,hierarchy,0,a);
                // Log.d("area", String.valueOf(area2));
                //  temp2=contours.get(contourIdx);
            }
            else
               areatotal=areatotal+area2;

        }
        Log.d("area"+cont, String.valueOf(areatotal));
        cont=cont+1;
        return tempp;
    }
}
