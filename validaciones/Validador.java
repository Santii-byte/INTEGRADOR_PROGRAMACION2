package validaciones;

import excepciones.IntNegativoExcepcion;
import excepciones.StringVacioExcepcion;

public class Validador {
    public static void validarStr(String str,String ent){
        if (str==null || str.isBlank()){
            throw new StringVacioExcepcion("No se permite "+ent+" vacio o nulo.");
        }
    }

    public static void validarInt(double i, String ent){
        if (i<0){
            throw new IntNegativoExcepcion("No se permite "+ent+" negativo");
        }
    }
}
