package demo.com.rounter.qr.decode;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

public class DecodeThread extends AsyncTask<Void, Void, Result> {
	private LuminanceSource luminanceSource;
	private DecodeListener listener;
	private Bitmap mBitmap;
	private boolean isStop = false;

	public DecodeThread(LuminanceSource luminanceSource, DecodeListener listener) {
		this.luminanceSource = luminanceSource;
		this.listener = listener;
	}

	@Override
	protected Result doInBackground(Void... params) {
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
        //设置解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(3);
		hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, listener);
		MultiFormatReader multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		long start = System.currentTimeMillis();
		Result rawResult = null;
		try {
			rawResult = multiFormatReader.decodeWithState(bitmap);
			mBitmap = luminanceSource.renderCroppedGreyScaleBitmap();
            saveBitmap(mBitmap);
            long end = System.currentTimeMillis();
			Log.d("DecodeThread", "Decode use " + (end - start) + "ms");
		} catch (ReaderException re) {
		} finally {
			multiFormatReader.reset();
		}
		return rawResult;
	}

	@Override
	protected void onPostExecute(Result result) {
		if (listener != null && !isStop) {
			if (result == null) {
				listener.onDecodeFailed(luminanceSource);
			} else {
				listener.onDecodeSuccess(result, luminanceSource, mBitmap);
			}
		}
	}

	public void cancel() {
		isStop = true;
		cancel(true);
    }

    public void saveBitmap(Bitmap bitmap) {
        File f = new File("/sdcard/", System.currentTimeMillis() + ".png");
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i("bmp", "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

	}
}
