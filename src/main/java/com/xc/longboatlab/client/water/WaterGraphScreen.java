package com.xc.longboatlab.client.water;

import com.xc.longboatlab.client.ClientJetSettings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

/** Read-only render graph with node-editor navigation, named sockets and live properties. */
public final class WaterGraphScreen extends Screen {
    private record Definition(String id,int x,int y,int color,String source,String[] inputs,String[] outputs) {}
    private record Link(int from,int output,int to,int input) {}
    private static String[] sockets(String... names){return names;}
    private static final int NODE_W=184,TOP=42,BOTTOM=18;
    private static final List<Definition> NODES=List.of(
            new Definition("water",0,0,0xff534761,"VanillaWaterSurface.java",sockets(),sockets("water","material")),
            new Definition("contact",0,182,0xff534761,"BoatWaterEffects.java",sockets(),sockets("motion","impact")),
            new Definition("pressure",224,0,0xff3d5655,"HeightfieldSurface.acceptHull",sockets("water","motion"),sockets("pressure")),
            new Definition("impact",224,192,0xff3d5655,"HeightfieldSurface.acceptImpact",sockets("impact"),sockets("pressure")),
            new Definition("field",448,0,0xff3d5655,"HeightfieldSurface.tick",sockets("pressure","pressure"),sockets("height")),
            new Definition("material",672,0,0xff5b5140,"water_surface.vsh / .fsh",sockets("height","material"),sockets("surface")),
            new Definition("ribbon",448,190,0xff425a40,"WaterSplashRenderer.WakeNode",sockets("motion"),sockets("geometry")),
            new Definition("foam",672,184,0xff5b5140,"WaterSplashRenderer.WakeStrip",sockets("geometry"),sockets("foam")),
            new Definition("splash",448,356,0xff425a40,"WaterSplashRenderer.Sheet",sockets("impact"),sockets("geometry")),
            new Definition("spray",672,350,0xff425a40,"WaterSplashRenderer.Fleck",sockets("geometry","geometry"),sockets("spray")),
            new Definition("output",900,130,0xff5c3939,"BoatEffectRenderPass",sockets("surface","foam","geometry","spray","gas"),sockets()),
            new Definition("jets",672,530,0xff5b5140,"BoatExhaust / JetSettings",sockets(),sockets("gas")));
    private static final List<Link> LINKS=List.of(new Link(0,0,2,0),new Link(1,0,2,1),new Link(1,1,3,0),
            new Link(2,0,4,0),new Link(3,0,4,1),new Link(4,0,5,0),new Link(0,1,5,1),
            new Link(1,0,6,0),new Link(6,0,7,0),new Link(1,1,8,0),new Link(6,0,9,0),new Link(8,0,9,1),
            new Link(5,0,10,0),new Link(7,0,10,1),new Link(8,0,10,2),new Link(9,0,10,3),new Link(11,0,10,4));
    private static final class Node {
        final Definition definition;double x,y;boolean collapsed;
        Node(Definition d){definition=d;x=d.x;y=d.y;}
        int height(){return collapsed?22:62+13*(definition.inputs.length+definition.outputs.length);}
        double socketY(boolean output,int index){return y+(collapsed?11:32+13*(index+(output?0:definition.outputs.length)));}
    }
    private final List<Node> nodes=new ArrayList<>();
    private double zoom=1,panX=20,panY=60,detailScroll;
    private int selected=4,canvasRight,canvasBottom,panelWidth,detailHeight,dragged=-1;
    private boolean panning,showSidebar=true,initialized;
    private BoatWaterEffects.Diagnostics stats;

    public WaterGraphScreen(){super(tr("title"));for(var d:NODES)nodes.add(new Node(d));}
    private static Text tr(String key,Object... args){return Text.translatable("screen.longboatlab.water."+key,args);}
    private final class HeaderButton extends ButtonWidget {
        HeaderButton(int x,int w,Text text,PressAction action){super(x,2,w,18,text,action,DEFAULT_NARRATION_SUPPLIER);}
        @Override protected void renderWidget(DrawContext c,int x,int y,float delta){
            if(isHovered()||isFocused())c.fill(getX(),getY(),getX()+getWidth(),getY()+getHeight(),0xff505050);
            String label=textRenderer.trimToWidth(getMessage().getString(),getWidth()-8);
            c.drawText(textRenderer,label,getX()+(getWidth()-textRenderer.getWidth(label))/2,getY()+5,0xffdddddd,false);
        }
    }
    private void layout(){
        panelWidth=showSidebar?Math.min(210,Math.max(144,width/4)):0;
        canvasRight=width-panelWidth;canvasBottom=height-BOTTOM;
    }
    @Override protected void init(){
        if(!initialized && width<600)showSidebar=false;
        layout();clearChildren();
        int x=8;
        addDrawableChild(new HeaderButton(x,72,tr("fit"),b->fit()));x+=74;
        addDrawableChild(new HeaderButton(x,60,tr("selected"),b->frameSelected()));x+=62;
        addDrawableChild(new HeaderButton(x,50,tr("properties"),b->{showSidebar=!showSidebar;layout();}));
        addDrawableChild(new HeaderButton(width-28,24,Text.literal("×"),b->close()));
        stats=BoatWaterEffects.diagnostics();
        if(!initialized){fit();initialized=true;}
    }
    private void fit(){
        double minX=Double.POSITIVE_INFINITY,minY=minX,maxX=Double.NEGATIVE_INFINITY,maxY=maxX;
        for(Node n:nodes){minX=Math.min(minX,n.x);minY=Math.min(minY,n.y);maxX=Math.max(maxX,n.x+NODE_W);maxY=Math.max(maxY,n.y+n.height());}
        zoom=MathHelper.clamp(Math.min((canvasRight-40)/(maxX-minX),(canvasBottom-TOP-40)/(maxY-minY)),0.12,1.1);
        panX=(canvasRight-(maxX-minX)*zoom)/2-minX*zoom;
        panY=TOP+(canvasBottom-TOP-(maxY-minY)*zoom)/2-minY*zoom;
    }
    private void frameSelected(){
        Node n=nodes.get(selected);zoom=Math.min(1.25,Math.max(0.6,(canvasRight-30.0)/(NODE_W+80)));
        panX=canvasRight/2.0-(n.x+NODE_W/2.0)*zoom;panY=(canvasBottom+TOP)/2.0-(n.y+n.height()/2.0)*zoom;
    }
    private void zoomAt(double value,double x,double y){
        double next=MathHelper.clamp(value,0.12,2.5);
        panX=x-(x-panX)*next/zoom;panY=y-(y-panY)*next/zoom;zoom=next;
    }
    private boolean inCanvas(double x,double y){return x>=0&&x<canvasRight&&y>=TOP&&y<canvasBottom;}
    private int nodeAt(double x,double y){
        double gx=(x-panX)/zoom,gy=(y-panY)/zoom;
        // Draw order is stable; the selected node is drawn on top and receives selection first.
        if(contains(nodes.get(selected),gx,gy))return selected;
        for(int i=nodes.size()-1;i>=0;i--)if(contains(nodes.get(i),gx,gy))return i;
        return -1;
    }
    private static boolean contains(Node n,double x,double y){return x>=n.x&&x<n.x+NODE_W&&y>=n.y&&y<n.y+n.height();}
    @Override public void tick(){stats=BoatWaterEffects.diagnostics();}
    @Override public boolean shouldPause(){return false;}
    @Override public Text getNarratedTitle(){var n=nodes.get(selected).definition;return title.copy().append(". ").append(tr(n.id+".title")).append(". ").append(tr(n.id+".description"));}
    @Override public void render(DrawContext c,int mx,int my,float delta){
        c.fill(0,0,width,height,0xff303030);c.fill(0,0,width,22,0xff292929);c.fill(0,22,width,TOP,0xff252525);
        c.drawText(textRenderer,tr("breadcrumb"),10,28,0xffb6b6b6,false);
        c.enableScissor(0,TOP,canvasRight,canvasBottom);
        // Coarsen the grid at low zoom instead of drawing thousands of subpixel dots.
        double spacing=24*zoom;while(spacing<12)spacing*=2;
        for(double x=positive(panX,spacing);x<canvasRight;x+=spacing)
            for(double y=TOP+positive(panY-TOP,spacing);y<canvasBottom;y+=spacing)
                c.fill((int)x,(int)y,(int)x+1,(int)y+1,0xff494949);
        c.getMatrices().push();c.getMatrices().translate(panX,panY,0);c.getMatrices().scale((float)zoom,(float)zoom,1);
        for(Link link:LINKS)wire(c,link);
        int hover=inCanvas(mx,my)?nodeAt(mx,my):-1;
        for(int i=0;i<nodes.size();i++)if(i!=selected)drawNode(c,nodes.get(i),false,i==hover);
        drawNode(c,nodes.get(selected),true,selected==hover);
        c.getMatrices().pop();c.disableScissor();
        if(showSidebar)details(c);
        c.fill(0,canvasBottom,width,height,0xff222222);
        String footer=tr(width<600?"controls_short":"controls").getString();
        c.drawText(textRenderer,textRenderer.trimToWidth(footer,width-12),6,height-12,0xffaaaaaa,false);
        if(width>640){String state=tr(stats.shader()?(stats.field()?"active":"waiting"):"fallback").getString();
            c.drawText(textRenderer,state,width-textRenderer.getWidth(state)-10,28,0xffbbbbbb,false);}
        super.render(c,mx,my,delta);
    }
    private static double positive(double a,double b){return(a%b+b)%b;}
    private static int socketColor(String type){return switch(type){
        case "geometry","motion","water"->0xff77bba0;
        case "material","surface","foam","gas","spray"->0xffd8c56a;
        default->0xffa8a8a8;};}
    private static void socket(DrawContext c,int x,int y,int color){
        c.fill(x-2,y-4,x+3,y+5,0xff191919);c.fill(x-4,y-2,x+5,y+3,0xff191919);
        c.fill(x-2,y-3,x+3,y+4,color);c.fill(x-3,y-2,x+4,y+3,color);
    }
    private void drawNode(DrawContext c,Node node,boolean chosen,boolean hover){
        Definition n=node.definition;int x=(int)node.x,y=(int)node.y,h=node.height();
        c.fill(x+2,y+3,x+NODE_W+3,y+h+4,0x55000000);
        c.fill(x,y,x+NODE_W,y+h,0xff404040);
        c.fill(x,y,x+NODE_W,y+21,n.color);
        c.drawBorder(x-1,y-1,NODE_W+2,h+2,chosen?0xffed9c45:hover?0xffb6b6b6:0xff1b1b1b);
        c.drawText(textRenderer,node.collapsed?">":"v",x+7,y+7,0xffdddddd,false);
        c.drawText(textRenderer,textRenderer.trimToWidth(tr(n.id+".title").getString(),NODE_W-27),x+21,y+7,0xffeeeeee,false);
        if(node.collapsed){if(n.inputs.length>0)socket(c,x,y+11,0xffa8a8a8);if(n.outputs.length>0)socket(c,x+NODE_W,y+11,0xffa8a8a8);return;}
        for(int i=0;i<n.outputs.length;i++){
            int sy=(int)node.socketY(true,i);String label=tr("socket."+n.outputs[i]).getString();
            socket(c,x+NODE_W,sy,socketColor(n.outputs[i]));
            c.drawText(textRenderer,label,x+NODE_W-10-textRenderer.getWidth(label),sy-4,0xffd7d7d7,false);
        }
        for(int i=0;i<n.inputs.length;i++){
            int sy=(int)node.socketY(false,i);socket(c,x,sy,socketColor(n.inputs[i]));
            c.drawText(textRenderer,tr("socket."+n.inputs[i]),x+10,sy-4,0xffd7d7d7,false);
        }
        c.fill(x+9,y+h-24,x+NODE_W-9,y+h-7,0xff343434);
        c.drawText(textRenderer,textRenderer.trimToWidth(metric(n.id).getString(),NODE_W-28),x+14,y+h-19,0xffbbbbbb,false);
    }
    private Text metric(String id){return switch(id){
        case "contact"->tr("metric.contact",stats.tracked(),stats.samples());
        case "pressure"->tr("metric.pressure",stats.hulls());
        case "field"->tr("metric.field");
        case "ribbon"->tr("metric.ribbon",stats.hullStrips(),stats.wakeStrips());
        case "splash"->tr("metric.splash",stats.splashes());
        case "spray"->tr("metric.spray",stats.drops(),stats.mist());
        case "foam"->tr("metric.foam",stats.ripples());
        case "jets"->tr("metric.jets",ClientJetSettings.density(),ClientJetSettings.force());
        default->tr(id+".metric");};}
    private void details(DrawContext c){
        Definition n=nodes.get(selected).definition;int x=canvasRight;
        c.fill(x,TOP,width,canvasBottom,0xff383838);c.fill(x,TOP,x+1,canvasBottom,0xff1e1e1e);
        c.enableScissor(x+1,TOP,width,canvasBottom);
        int y=TOP+10-(int)detailScroll;
        c.fill(x+6,y-4,width-6,y+15,0xff2d2d2d);
        y=paragraph(c,tr("node"),y,x,0xffdddddd)+12;
        y=paragraph(c,tr(n.id+".title"),y,x,0xffeeeeee)+8;
        y=paragraph(c,metric(n.id),y,x,0xffbbbbbb)+12;
        y=paragraph(c,tr(n.id+".description"),y,x,0xffcccccc)+14;
        y=paragraph(c,tr("source",n.source),y,x,0xffa6a6a6);
        detailHeight=y-(TOP+10)+(int)detailScroll;c.disableScissor();
    }
    private int paragraph(DrawContext c,Text t,int y,int x,int color){
        for(var line:textRenderer.wrapLines(t,panelWidth-22)){c.drawText(textRenderer,line,x+11,y,color,false);y+=12;}return y;
    }
    private void wire(DrawContext c,Link link){
        Node a=nodes.get(link.from),b=nodes.get(link.to);
        double x0=a.x+NODE_W,y0=a.socketY(true,link.output),x3=b.x,y3=b.socketY(false,link.input);
        double bend=Math.max(40,Math.abs(x3-x0)*0.5),px=x0,py=y0;
        int color=link.from==selected||link.to==selected?0xffc3c3c3:0xff818181;
        for(int i=1;i<=24;i++){
            double t=i/24.0,u=1-t;
            double x=u*u*u*x0+3*u*u*t*(x0+bend)+3*u*t*t*(x3-bend)+t*t*t*x3;
            double y=u*u*u*y0+3*u*u*t*y0+3*u*t*t*y3+t*t*t*y3;
            var m=c.getMatrices();m.push();m.translate(px,py,0);m.multiply(RotationAxis.POSITIVE_Z.rotation((float)Math.atan2(y-py,x-px)));
            c.fill(0,-1,(int)Math.ceil(Math.hypot(x-px,y-py))+1,1,color);m.pop();px=x;py=y;
        }
    }
    @Override public boolean mouseClicked(double x,double y,int button){
        if(inCanvas(x,y)&&(button==0||button==2)){
            int hit=nodeAt(x,y);
            if(hit>=0&&button==0){selected=hit;detailScroll=0;Node n=nodes.get(hit);
                if((x-panX)/zoom<n.x+18&&(y-panY)/zoom<n.y+22)n.collapsed=!n.collapsed;
                else dragged=hit;
            }else panning=true;
            return true;
        }
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){
        if(dragged>=0){Node n=nodes.get(dragged);n.x+=dx/zoom;n.y+=dy/zoom;return true;}
        if(panning){panX+=dx;panY+=dy;return true;}
        return super.mouseDragged(x,y,button,dx,dy);
    }
    @Override public boolean mouseReleased(double x,double y,int button){
        if(panning||dragged>=0){panning=false;dragged=-1;return true;}return super.mouseReleased(x,y,button);
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(inCanvas(x,y)){zoomAt(zoom*Math.pow(1.13,vertical),x,y);return true;}
        if(showSidebar&&x>=canvasRight&&y>=TOP&&y<canvasBottom){
            detailScroll=MathHelper.clamp(detailScroll-vertical*22,0,Math.max(0,detailHeight-(canvasBottom-TOP)+20));return true;}
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(key==GLFW.GLFW_KEY_HOME){fit();return true;}
        if(key==GLFW.GLFW_KEY_N){showSidebar=!showSidebar;layout();return true;}
        if(key==GLFW.GLFW_KEY_KP_DECIMAL){frameSelected();return true;}
        if(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT){
            selected=Math.floorMod(selected+(key==GLFW.GLFW_KEY_RIGHT?1:-1),nodes.size());detailScroll=0;frameSelected();return true;}
        return super.keyPressed(key,scan,modifiers);
    }
}
