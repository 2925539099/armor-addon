package com.armoraddon.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/**
 * 超重型甲的盔甲模型。
 *
 * <p>作者用 Blockbench 给这套甲加了原版没有的装饰方块 —— 两个肩甲、两块膝盖护板。
 * 它们不是原版方块，UV 由作者逐个指定，所以不能直接用原版 {@link HumanoidModel}
 * 渲染，也不能四套共用一份 UV（四套的 bbmodel 是分别画的，位置并不一致）。</p>
 *
 * <h2>模型坐标</h2>
 * <p>Minecraft 的模型 y 轴<strong>向下为正</strong>，根的原点在世界 y=24（肩部）。
 * 作者的 bbmodel 用的是常规的「y=0 在脚底」坐标系，换算时用
 * {@code 局部 y = 24 - 世界 y}；挂在手臂或腿上的方块则再减去该部件
 * {@code PartPose.offset} 的 y。</p>
 *
 * <h2>模型膨胀</h2>
 * <p>原版盔甲层会把模型整体膨胀一点，好让盔甲包在皮肤外面：外层用
 * {@code CubeDeformation(1.0F)}（{@code LayerDefinitions.OUTER_ARMOR_DEFORMATION}），
 * 内层用 {@code (0.5F)}。本类沿用这两个标准值。</p>
 *
 * <p><strong>每个 {@code addBox} 都必须显式传 deformation。</strong>只给
 * {@code createMesh} 传是不够的 —— 原版 {@code createMesh} 里也是逐块传的。
 * 漏传的话那些方块不会跟着膨胀，肩甲会比手臂小一圈。</p>
 *
 * <p>膨胀同时替膝盖护板解决了厚度问题：它本身 1 格厚，膨胀 0.5 后视觉上是 2 格，
 * 而且不会破坏 UV 布局。直接把厚度参数从 1 改成 2 会让侧面只有 1 像素宽的图案
 * 被拉成 2 格、相邻像素串进来，看起来就是材质错乱。</p>
 */
public class UltraHeavyArmorModel extends HumanoidModel<LivingEntity> {
	public UltraHeavyArmorModel(ModelPart root) {
		super(root);
	}

	/**
	 * 把一个部件换成空的 —— 保留占位，但渲染时不画任何东西。
	 *
	 * <p>不能靠 {@code part.visible = false} 来隐藏：{@code HumanoidArmorLayer}
	 * 每次渲染都会先 {@code setAllVisible(false)}，再按槽位把需要的部件逐个设成
	 * {@code visible = true}，头盔那一支会连 {@code hat} 一起打开：</p>
	 *
	 * <pre>
	 * p_117126_.setAllVisible(false);
	 * switch (slot) {
	 *     case HEAD -&gt; {
	 *         p_117126_.head.visible = true;
	 *         p_117126_.hat.visible = true;   // 把我们的设置覆盖掉
	 *     }
	 * }
	 * </pre>
	 *
	 * <p>所以只能从模型定义入手，让它没有方块可画。名称必须保留，
	 * 因为 {@link HumanoidModel} 的构造会 {@code getChild("hat")}。</p>
	 */
	private static void emptyPart(PartDefinition root, String name) {
		root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
	}

	/**
	 * 外层：头盔 / 胸甲 / 靴子，外加两个肩甲。
	 *
	 * @param leftPadU  左肩甲的 texOffs u
	 * @param leftPadV  左肩甲的 texOffs v
	 * @param rightPadU 右肩甲的 texOffs u
	 * @param rightPadV 右肩甲的 texOffs v
	 */
	public static LayerDefinition createOuterLayer(int leftPadU, int leftPadV,
												   int rightPadU, int rightPadV) {
		CubeDeformation deformation = new CubeDeformation(1.0F);
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0.0F);
		PartDefinition root = mesh.getRoot();

		// 原版的 hat 比 head 大 0.5，渲染头盔时会被一起打开，
		// 表现成头盔外面多一层壳。作者的 bbmodel 里没有这个，去掉。
		emptyPart(root, "hat");

		// 肩甲的世界坐标是 x 5.5~8.5（左）与 -8.5~-5.5（右）、y 20~25、z -2.5~2.5。
		// 左右手臂的 PartPose.offset 是 (±5, 2, 0)，根的 y 是 24，
		// 于是左肩是 addBox(0.5, -3, -2.5, 3, 5, 5)、右肩是 addBox(-3.5, -3, -2.5, ...)。
		//
		// 两个肩甲都要 mirror()：ModelPart.Polygon 把顶点映射到 UV 矩形四个角的顺序，
		// 与 Blockbench 预览时相反，不加的话每个面的图案在面内是左右镜像的。
		root.getChild("left_arm").addOrReplaceChild(
				"left_shoulder_pad",
				CubeListBuilder.create().mirror().texOffs(leftPadU, leftPadV)
						.addBox(0.5F, -3.0F, -2.5F, 3, 5, 5, deformation),
				PartPose.ZERO);

		root.getChild("right_arm").addOrReplaceChild(
				"right_shoulder_pad",
				CubeListBuilder.create().mirror().texOffs(rightPadU, rightPadV)
						.addBox(-3.5F, -3.0F, -2.5F, 3, 5, 5, deformation),
				PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 32);
	}

	/**
	 * 内层：护腿，外加两块膝盖护板。
	 *
	 * @param rightPlateU 右腿护板的 texOffs u
	 * @param rightPlateV 右腿护板的 texOffs v
	 * @param leftPlateU  左腿护板的 texOffs u
	 * @param leftPlateV  左腿护板的 texOffs v
	 */
	public static LayerDefinition createInnerLayer(int rightPlateU, int rightPlateV,
												   int leftPlateU, int leftPlateV) {
		CubeDeformation deformation = new CubeDeformation(0.5F);
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0.0F);
		PartDefinition root = mesh.getRoot();

		emptyPart(root, "hat");

		// 护板的世界坐标原本是 x -4.9~1.1（右腿）与 -1.15~4.85（左腿）、y 5~9、
		// z -3.5~-2.5。左右腿的 PartPose.offset 是 (±1.9, 12, 0)。
		//
		// 宽度从 6 收到了 4：腿本身是 4 宽、内层再膨胀 0.5，视觉宽度是 5；
		// 护板原来 6 宽、膨胀后 7，每边比腿多出一格，看着太宽。
		// 改成 4 宽、x 起点同步从 -3 挪到 -2 之后，两块都是 x -2~2，
		// 膨胀 0.5 后是 -2.5~2.5 —— 与腿完全对齐，视觉宽度同为 5。
		//
		// 同样要 mirror()，理由与肩甲相同。
		root.getChild("right_leg").addOrReplaceChild(
				"right_knee_plate",
				CubeListBuilder.create().mirror().texOffs(rightPlateU, rightPlateV)
						.addBox(-2.0F, 3.0F, -3.5F, 4, 4, 1, deformation),
				PartPose.ZERO);

		root.getChild("left_leg").addOrReplaceChild(
				"left_knee_plate",
				CubeListBuilder.create().mirror().texOffs(leftPlateU, leftPlateV)
						.addBox(-2.0F, 3.0F, -3.5F, 4, 4, 1, deformation),
				PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 32);
	}
}
