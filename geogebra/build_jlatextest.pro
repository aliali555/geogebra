#
# Proguard config file for GeoGebra
#
#
-injars ../build/geogebra.jar
-injars ../build/geogebra_main.jar
-injars ../build/geogebra_gui.jar
-injars ../build/geogebra_export.jar
-injars ../build/geogebra_cas.jar
-injars ../build/geogebra_3d.jar
-injars ../build/jlatexmath.jar

-outjars ../build/temp

# libraries
-libraryjars ../java150-rt.jar
-libraryjars lib_jsobject.jar
-libraryjars lib_mac_extensions.jar
-libraryjars 3D/jogl.jar
-libraryjars 3D/gluegen-rt.jar
#-libraryjars jlatexmath.jar

# Rhino Javascript is not obfuscated
-libraryjars ../build/geogebra_javascript.jar


-dontoptimize
-allowaccessmodification
-overloadaggressively

# needed for eg StringBuilder.setLength()
# see http://proguard.sourceforge.net/manual/troubleshooting.html
-dontskipnonpubliclibraryclasses

#-printmapping geogebra3-2-0-0.map 	 
#-applymapping geogebra3-2-0-0.map	 

# Keep GeoGebra application
-keep class geogebra.GeoGebra {
    public static void main(java.lang.String[]);
}


# Keep GeoGebra3D application
-keep class geogebra.GeoGebra3D {
    public static void main(java.lang.String[]);
}

# Keep GeoGebra applet
-keep class geogebra.GeoGebraApplet {
    public <methods>;
}


# Keep GeoGebraAppletPreloader
-keep class geogebra.GeoGebraAppletPreloader {
    public <methods>;
}

# Keep GeoGebraPanel
-keep class geogebra.GeoGebraPanel {
    public <methods>;
}

# see META-INF/services
-keep class org.freehep.graphicsio.raw.RawImageWriterSpi { <methods>; }

# needed so that hoteqn can find Des12.gif, etc.
-keep class geogebra.gui.hoteqn.SymbolLoader { <methods>; }

# 
-keep class org.scilab.forge.jlatexmath.* { <methods>; }
-keep class org.scilab.forge.jlatexmath.greek.* { <methods>; }
-keep class org.scilab.forge.jlatexmath.cyrillic.* { <methods>; }

# Keep 
#-keep class org.scilab.forge.jlatexmath.predefMacros {
#    public <methods>;
#}

# Jasymca uses reflection to create functions like LambaSIN
-keep class jasymca.Lambda* {}

# Rhino Javascript
#-keep class org.mozilla.classfile.* {  }
#-keep class org.mozilla.javascript.* { }
#-keep class org.mozilla.javascript.jdk13.* { }
#-keep class org.mozilla.javascript.jdk15.* {  }
#-keep class org.mozilla.javascript.optimizer.* { }
#-keep class org.mozilla.javascript.regexp.* {  }
#-keep class org.mozilla.javascript.serialize.* {  }
#-keep class org.mozilla.javascript.xml.* {  }

# supress foxtrot error messages
-keep class java.util.LinkedList { java.lang.Object getFirst(); }
-keep class java.lang.Object { java.lang.Object list; }

-keep class geogebra.gui.virtualkeyboard.VirtualKeyboard { public static void main(java.lang.String[]); }



#####
# Plugin part
####

-keep class geogebra.plugin.GgbAPI { <methods>; }

# -keep public class * {
#    public protected *;
# }

#-keep class geogebra.gui.util.BrowserLauncher { <methods>; }
-keep class geogebra.plugin.PlugLetIF { <methods>; }
#-keep class geogebra.MyFileFilter { <methods>; }

#-keep class geogebra.Application { <methods>; }
#-keep class geogebra.kernel.Construction { <methods>; }
#-keep class geogebra.kernel.ConstructionElement { <methods>; }
#-keep class geogebra.kernel.AlgoElement { <methods>; }
#-keep class geogebra.kernel.arithmetic.Equation { <methods>; }
#-keep class geogebra.kernel.arithmetic.ExpressionNode { <methods>; }
#-keep class geogebra.kernel.arithmetic.ExpressionValue { <methods>; }
#-keep class geogebra.kernel.arithmetic.Function { <methods>; }
#-keep class geogebra.kernel.arithmetic.NumberValue { <methods>; }
#-keep class geogebra.kernel.Dilateable { <methods>; }
#-keep class geogebra.kernel.GeoBoolean { <methods>; }
#-keep class geogebra.kernel.GeoConic { <methods>; }
#-keep class geogebra.kernel.GeoCurveCartesian { <methods>; }
#-keep class geogebra.kernel.GeoDeriveable { <methods>; }
#-keep class geogebra.kernel.GeoElement { <methods>; }
#-keep class geogebra.kernel.GeoFunction { <methods>; }
#-keep class geogebra.kernel.GeoImage { <methods>; }
#-keep class geogebra.kernel.GeoLine { <methods>; }
#-keep class geogebra.kernel.GeoList { <methods>; }
#-keep class geogebra.kernel.GeoNumeric { <methods>; }
#-keep class geogebra.kernel.GeoPoint { <methods>; }
#-keep class geogebra.kernel.GeoPolygon { <methods>; }
#-keep class geogebra.kernel.GeoSegment { <methods>; }
#-keep class geogebra.kernel.GeoText { <methods>; }
#-keep class geogebra.kernel.GeoVec3D { <methods>; }
#-keep class geogebra.kernel.GeoVector { <methods>; }
#-keep class geogebra.kernel.Kernel { <methods>; }
#-keep class geogebra.kernel.Macro { <methods>; }
#-keep class geogebra.kernel.Mirrorable { <methods>; }
#-keep class geogebra.kernel.Path { <methods>; }
#-keep class geogebra.kernel.PointRotateable { <methods>; }
#-keep class geogebra.kernel.Rotateable { <methods>; }
#-keep class geogebra.kernel.Translateable { <methods>; }
#-keep class org.freehep.graphics2d.TagString { <methods>; }
