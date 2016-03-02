package org.redfrog404.mobycraft.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelDockerWhale extends ModelBase
{
  //fields
    ModelRenderer BodyBottom;
    ModelRenderer BodyMiddle;
    ModelRenderer BodyTop;
    ModelRenderer TailMiddle;
    ModelRenderer TailLeft;
    ModelRenderer TailRight;
    ModelRenderer Box1;
    ModelRenderer Box2;
    ModelRenderer Box3;
    ModelRenderer Box4;
    ModelRenderer Box5;
    ModelRenderer Box6;
    ModelRenderer Box7;
  
  public ModelDockerWhale()
  {
    textureWidth = 128;
    textureHeight = 128;
    
      BodyBottom = new ModelRenderer(this, 0, 42);
      BodyBottom.addBox(0F, 0F, 0F, 24, 2, 16);
      BodyBottom.setRotationPoint(-12F, 22F, -8F);
      BodyBottom.setTextureSize(128, 128);
      BodyBottom.mirror = true;
      setRotation(BodyBottom, 0F, 0F, 0F);
      BodyMiddle = new ModelRenderer(this, 0, 22);
      BodyMiddle.addBox(0F, 0F, 0F, 36, 4, 16);
      BodyMiddle.setRotationPoint(-18F, 18F, -8F);
      BodyMiddle.setTextureSize(128, 128);
      BodyMiddle.mirror = true;
      setRotation(BodyMiddle, 0F, 0F, 0F);
      BodyTop = new ModelRenderer(this, 0, 0);
      BodyTop.addBox(0F, 0F, 0F, 48, 6, 16);
      BodyTop.setRotationPoint(-24F, 12F, -8F);
      BodyTop.setTextureSize(128, 128);
      BodyTop.mirror = true;
      setRotation(BodyTop, 0F, 0F, 0F);
      TailMiddle = new ModelRenderer(this, 0, 60);
      TailMiddle.addBox(0F, 0F, 0F, 6, 4, 4);
      TailMiddle.setRotationPoint(-30F, 11F, -2F);
      TailMiddle.setTextureSize(128, 128);
      TailMiddle.mirror = true;
      setRotation(TailMiddle, 0F, 0F, 0F);
      TailLeft = new ModelRenderer(this, 20, 60);
      TailLeft.addBox(0F, 0F, 0F, 5, 4, 5);
      TailLeft.setRotationPoint(-32F, 9F, 2F);
      TailLeft.setTextureSize(128, 128);
      TailLeft.mirror = true;
      setRotation(TailLeft, 0F, 0F, 0F);
      TailRight = new ModelRenderer(this, 20, 60);
      TailRight.addBox(0F, 0F, 0F, 5, 4, 5);
      TailRight.setRotationPoint(-32F, 9F, -7F);
      TailRight.setTextureSize(128, 128);
      TailRight.mirror = true;
      setRotation(TailRight, 0F, 0F, 0F);
      Box1 = new ModelRenderer(this, 40, 60);
      Box1.addBox(0F, 0F, 0F, 6, 6, 6);
      Box1.setRotationPoint(11F, 6F, -6F);
      Box1.setTextureSize(128, 128);
      Box1.mirror = true;
      setRotation(Box1, 0F, 0F, 0F);
      Box2 = new ModelRenderer(this, 40, 60);
      Box2.addBox(0F, 0F, 0F, 6, 6, 6);
      Box2.setRotationPoint(0F, 0F, 0F);
      Box2.setTextureSize(128, 128);
      Box2.mirror = true;
      setRotation(Box2, 0F, 0F, 0F);
      Box3 = new ModelRenderer(this, 40, 60);
      Box3.addBox(0F, 0F, 0F, 6, 6, 6);
      Box3.setRotationPoint(-21F, 6F, -1F);
      Box3.setTextureSize(128, 128);
      Box3.mirror = true;
      setRotation(Box3, 0F, 0F, 0F);
      Box4 = new ModelRenderer(this, 40, 60);
      Box4.addBox(0F, 0F, 0F, 6, 6, 6);
      Box4.setRotationPoint(0F, 6F, 0F);
      Box4.setTextureSize(128, 128);
      Box4.mirror = true;
      setRotation(Box4, 0F, 0F, 0F);
      Box5 = new ModelRenderer(this, 40, 60);
      Box5.addBox(0F, 0F, 0F, 6, 6, 6);
      Box5.setRotationPoint(-11F, -6F, -4F);
      Box5.setTextureSize(128, 128);
      Box5.mirror = true;
      setRotation(Box5, 0F, 0F, 0F);
      Box6 = new ModelRenderer(this, 40, 60);
      Box6.addBox(0F, 0F, 0F, 6, 6, 6);
      Box6.setRotationPoint(-11F, 0F, -4F);
      Box6.setTextureSize(128, 128);
      Box6.mirror = true;
      setRotation(Box6, 0F, 0F, 0F);
      Box7 = new ModelRenderer(this, 40, 60);
      Box7.addBox(0F, 0F, 0F, 6, 6, 6);
      Box7.setRotationPoint(-11F, 6F, -4F);
      Box7.setTextureSize(128, 128);
      Box7.mirror = true;
      setRotation(Box7, 0F, 0F, 0F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    BodyBottom.render(f5);
    BodyMiddle.render(f5);
    BodyTop.render(f5);
    TailMiddle.render(f5);
    TailLeft.render(f5);
    TailRight.render(f5);
    Box1.render(f5);
    Box2.render(f5);
    Box3.render(f5);
    Box4.render(f5);
    Box5.render(f5);
    Box6.render(f5);
    Box7.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
  
  public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
  {
    super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
  }

}