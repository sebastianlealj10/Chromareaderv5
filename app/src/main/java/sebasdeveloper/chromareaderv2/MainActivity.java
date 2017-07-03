package sebasdeveloper.chromareaderv2;
//clases que se usan en esta actividad
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.bitmap;

//Clase de la vista para la actividad principal
public class MainActivity extends AppCompatActivity
{
    //Clase para inicializar la libreria opencv
    private static final String TAG = "MainActivity";

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    Uri imageUri;
    Intent i;
    Intent intent;
    Bitmap bmp;
    Bitmap bmp2;
    Mat ima;
    @BindView(R.id.image) ImageView img;
    @BindView(R.id.button2) Button btn2;
    final static int captureimage=0;
    final static int loadimage=1;
    @Override
    //Clase donde se crea el layout y se inicializa la libreria ButterKnife
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
    //Opcional
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
    //Clase donde se muestra la foto en el imageview provenga de la camara o de la galeria
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //Condicion para la camara
        if(resultCode == Activity.RESULT_OK  &&  requestCode == captureimage)
        {
            //funciones del opencv
            ima =imread_mat();
            ima=rotateima(ima);
            imwrite_mat(ima);
            showima();
            btn2.setEnabled(true);
        }
        //Condicion para la galeria
        if(resultCode == Activity.RESULT_OK  &&  requestCode == loadimage)
        {
            imageUri = data.getData();
            //se contruye la imagen en bitmap a partir del uri de la imagen
            try {
                bmp2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //se graba el bitmap en un directorio externo
            try {
                FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory()+
                        "/sebas/"+"cromaoriginal.jpg");
                bmp2.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            img.setImageBitmap(bmp2);
            btn2.setEnabled(true);

        }
    }
    //Boton donde se captura la foto y se guarda en la memoria
    @OnClick(R.id.button)
    public void takephoto(View view)
    {
        //Creamos el Intent para llamar a la Camara
        Intent cameraIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        //Creamos una carpeta en la memeria del terminal
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "sebas");
        imagesFolder.mkdirs();
        //añadimos el nombre de la imagen
        File image = new File(imagesFolder, "cromaoriginal.jpg");
        Uri uriSavedImage = Uri.fromFile(image);
        //Le decimos al Intent que queremos grabar la imagen
        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uriSavedImage);
        //Lanzamos la aplicacion de la camara con retorno (forResult)

        startActivityForResult(cameraIntent, captureimage);

    }
    // Boton donde se lanza la nueva actividad
    @OnClick(R.id.button2)
    public void processingphoto(View view)
    {
        intent = new Intent(this, PreprocessingActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.button3)
    public void loadimage(View view)
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

        startActivityForResult(gallery, loadimage);
    }
    public Mat imread_mat(){
        Mat imagen;
        //Se lee la foto desde la ubicacion donde fue almacenada en la memoria interna
        imagen = Imgcodecs.imread(Environment.getExternalStorageDirectory()+
                "/sebas/"+"foto1.jpg");
        return imagen;
    }
    public void imwrite_mat(Mat imagen){
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory()+
                "/sebas/"+"foto1.jpg",imagen);
    }
    public void showima(){
        bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+
                "/sebas/"+"foto1.jpg");
        img.setImageBitmap(bmp);
    }
    public Mat rotateima(Mat imagen){
        Core.transpose(imagen,imagen);
        return imagen;
    }

}