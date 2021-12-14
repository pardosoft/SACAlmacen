package com.proveedoradeclimas.sacalmacen.clases;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import com.proveedoradeclimas.sacalmacen.R;

import java.util.HashMap;

/**
 * Created by Administrador on 12/09/2016.
 */
public class zMisSonidos {

    public static int Error = R.raw.slap;
    public static int Scan = R.raw.scan2;
    public static int Wrong = R.raw.wrong2;
    public static int Pop = R.raw.pop;
    public static int Alert = R.raw.alert;
    public static int NO = R.raw.no;
    public static int Success = R.raw.success1;
    public static int Success2 = R.raw.success2;

    private static SoundPool soundPool;
    private static HashMap<Integer, Integer> soundPoolMap;

    @SuppressLint("UseSparseArrays")
    private static void initSounds(Context context)
    {
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPoolMap = new HashMap<Integer, Integer>(6);

        soundPoolMap.put(Error, soundPool.load(context, R.raw.slap, 1));
        soundPoolMap.put(Scan, soundPool.load(context, R.raw.scan2, 2));
        soundPoolMap.put(Wrong, soundPool.load(context, R.raw.wrong2, 3));
        soundPoolMap.put(Pop, soundPool.load(context, R.raw.pop, 4));
        soundPoolMap.put(Alert, soundPool.load(context, R.raw.alert, 5));
        soundPoolMap.put(NO, soundPool.load(context, R.raw.no, 6));
        soundPoolMap.put(Success, soundPool.load(context, R.raw.success1, 7));
        soundPoolMap.put(Success2, soundPool.load(context, R.raw.success2, 8));
    }

    /** Play a given sound in the soundPool */
    public static void playSound(Context context, int soundID) {
        if(soundPool == null || soundPoolMap == null){
            initSounds(context);
        }

        soundPool.play((Integer)soundPoolMap.get(soundID), 1, 1, 1, 0, 1f);

        if (soundID == Error || soundID == Wrong || soundID == Alert || soundID == NO)
        {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            v.vibrate(800);
        }
    }
}
