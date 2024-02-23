package eu.pb4.ansharpatch.mixin;

import com.lgmrszd.anshar.beacon.BeaconNode;
import com.lgmrszd.anshar.frequency.FrequencyNetwork;
import com.lgmrszd.anshar.transport.PlayerTransportComponent;
import com.lgmrszd.anshar.transport.TransportEffects;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;


@Mixin(PlayerTransportComponent.class)
public abstract class PlayerTransportComponentMixin implements HolderAttachment {
    @Shadow
    @Final
    private PlayerEntity player;
    @Shadow
    private @Nullable BeaconNode target;

    @Shadow
    public abstract Set<BeaconNode> getJumpCandidates();

    @Shadow
    public abstract BeaconNode getNearestLookedAt();

    @Shadow
    public abstract boolean tryJump(BeaconNode node);

    @Shadow
    protected abstract void moveToCurrentTarget();

    @Shadow
    public abstract double distanceTo(BeaconNode node);

    @Shadow
    public abstract Vector3f compassNormToNode(BeaconNode node);

    @Shadow public abstract boolean isInNetwork();

    @Shadow public abstract void serverTick();

    @Unique
    private static final int TICKS_TO_JUMP = 230;
    @Unique
    private final ElementHolder holder = new ElementHolder();
    @Unique
    private final BlockDisplayElement source = new BlockDisplayElement();
    @Unique
    private final Random random = Random.create();
    @Unique
    private boolean done = false;
    @Unique
    private boolean isSetup = false;
    @Unique
    private ServerPlayerEntity serverPlayer;
    @Unique
    private int jumpCooldown = 0;
    @Unique
    private int gateTicks = 0;
    @Unique
    @Nullable
    private BeaconNode nearest = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(PlayerEntity player, CallbackInfo ci) {
        holder.addElement(source);
        var i = new ItemDisplayElement(Items.BLACK_CONCRETE);
        i.setModelTransformation(ModelTransformationMode.FIXED);
        i.setScale(new Vector3f(-100));
        i.setBrightness(new Brightness(0, 0));
        holder.addElement(i);
        holder.setAttachment(this);
        if (player instanceof ServerPlayerEntity serverPlayer) {
            this.serverPlayer = serverPlayer;
        }
    }

    @Inject(method = "enterNetwork", at = @At("TAIL"))
    private void onEnter(FrequencyNetwork network, BlockPos through, CallbackInfo ci) {
        if (this.serverPlayer == null) {
            return;
        }
        this.moveToCurrentTarget();
        this.setupState();
    }

    @Unique
    private void setupState() {
        this.isSetup = true;
        var list = new IntArrayList(source.getEntityIds());
        list.rem(this.source.getEntityId());
        list.add(this.player.getId());
        this.holder.startWatching(this.serverPlayer);
        this.serverPlayer.networkHandler.sendPacket(VirtualEntityUtils.createRidePacket(this.source.getEntityId(), list));
        this.serverPlayer.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, GameMode.SPECTATOR.getId()));
    }

    @Inject(method = "exitNetwork", at = @At("HEAD"), remap = false)
    private void onExit(CallbackInfo ci) {
        if (this.serverPlayer == null) {
            return;
        }
        var list = new IntArrayList(source.getEntityIds());
        list.rem(this.source.getEntityId());
        this.serverPlayer.networkHandler.sendPacket(VirtualEntityUtils.createRidePacket(this.source.getEntityId(), list));
        this.holder.stopWatching(this.serverPlayer);
        this.serverPlayer.sendMessage(Text.empty(), true);
        this.serverPlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.empty()));
        this.serverPlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.empty()));
        this.serverPlayer.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, this.serverPlayer.interactionManager.getGameMode().getId()));
    }

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lcom/lgmrszd/anshar/transport/PlayerTransportComponent;moveToCurrentTarget()V"), remap = false)
    public void onTick(CallbackInfo ci) {
        if (this.serverPlayer == null) {
            return;
        }
        if (!this.isSetup) {
            this.setupState();
        }

        if (!this.done) {
            if (this.jumpCooldown > 0) {
                --this.jumpCooldown;
            } else {
                this.holder.tick();
                if (this.player.forwardSpeed > 0.1) {
                    if (this.nearest == null && this.gateTicks == 0 && this.player.getWorld().getTime() % 10L == 0L) {
                        this.nearest = this.getNearestLookedAt();
                        if (this.nearest != null) {
                            //this.audioManager.playJump();
                        }
                    }

                    if (this.nearest != null) {
                        if (this.gateTicks % 40 == 0) {
                            this.serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_BEACON_AMBIENT), SoundCategory.BLOCKS,
                                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 0.8f, 2f, 0l));
                        }
                        ++this.gateTicks;

                    }
                } else {
                    this.gateTicks = 0;
                    this.nearest = null;
                    //this.audioManager.stopJump();
                    //this.spawnOrientationParticles();
                    this.serverPlayer.networkHandler.sendPacket(new StopSoundS2CPacket(SoundEvents.BLOCK_BEACON_AMBIENT.getId(), SoundCategory.BLOCKS));
                }
                var helpColr = 7368816;

                BeaconNode target;

                if (this.nearest == null) {
                    target = this.getNearestLookedAt();
                    for (var node : this.getJumpCandidates()) {
                        this.drawGate(node, false, this.gateTicks);
                    }
                } else {
                    target = this.nearest;
                    this.drawGate(this.nearest, true, this.gateTicks);
                }

                Text name;
                Text pos;

                if (target != null) {
                    name = Text.empty().append(target.getName()).withColor(target.getColorHex());
                    pos = Text.literal(target.getPos().toShortString()).append(" (" + (int) this.distanceTo(target) + ")").withColor(16777215);
                } else {
                    name = Text.empty();
                    pos = Text.empty();
                }
                this.serverPlayer.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 10, 0));
                this.serverPlayer.sendMessage(Text.translatable("anshar.help.transport.location", this.target.getName()).withColor(helpColr), true);
                this.serverPlayer.networkHandler.sendPacket(new TitleS2CPacket(name));
                this.serverPlayer.networkHandler.sendPacket(new SubtitleS2CPacket(pos));

                if (this.gateTicks >= 230) {
                    this.tryJump(this.nearest);
                    this.holder.tick();
                    //this.audioManager.stopJump();
                    this.gateTicks = 0;
                    this.nearest = null;
                    this.jumpCooldown = 10;
                    //this.audioManager.reset();
                } else {
                    //this.audioManager.tick();
                }

            }
        }
    }

    private void drawGate(BeaconNode node, boolean nearest, int ticks) {
        Vector3f normalGirl = this.compassNormToNode(node);
        float jumpRatio = (float) ticks / 230.0F;
        float intensity = !nearest ? 1.0F : 1.0F + jumpRatio;
        Matrix3f M = (new Matrix3f()).setLookAlong(normalGirl, new Vector3f(0.0F, 1.0F, 0.0F)).invert();
        Vector3f normalExt = normalGirl.mul(20.0F, new Vector3f());
        float gateWidth = 3.0F * (intensity - 1.0F);
        float gateHeight = 10.0F * (float) Math.pow(intensity, 4.0);
        float starSpeed = 0.1F * ((float) Math.pow(intensity, 4.0) - 1.0F);


        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 3 * (int) Math.pow(intensity, 4.0); ++i) {
                float pY = random.nextFloat() * gateHeight - gateHeight / 2.0F;
                Vector3f pPos = (new Vector3f(gateWidth * (float) side, pY, 0.0F)).mul(M).add(normalExt).add(player.getEyePos().toVector3f());
                Vector3f pVel = (new Vector3f(0.0F, 0.0F, starSpeed)).mul(M);
                float[] colors = node.getColor();
                var color = new Vector3f();
                if (nearest) {
                    color.set(colors[0] + jumpRatio * (random.nextFloat() - colors[0]), colors[1] + jumpRatio * (random.nextFloat() - colors[1]), colors[2] + jumpRatio * (random.nextFloat() - colors[2]));
                } else {
                    color.set(colors[0], colors[1], colors[2]);
                }

                var e = random.nextFloat() < jumpRatio ? ParticleTypes.FIREWORK : new DustParticleEffect(color, 15);
                serverPlayer.networkHandler.sendPacket(new ParticleS2CPacket(e, true, pPos.x, pPos.y, pPos.z, pVel.x, pVel.y, pVel.z, 1, 0));
            }
        }
    }

    public float getJumpPercentage() {
        return (float) this.gateTicks / 230.0F;
    }

    @Redirect(method = "moveToCurrentTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;teleport(DDD)V"))
    private void replaceTeleport(PlayerEntity instance, double x, double y, double z) {
        instance.setPos(x, y, z);
    }

    /**
     * @author Patbox
     * @reason Server sidin
     */
    @Overwrite
    public void sendExplosionPacketS2C(boolean skipOurselves, BlockPos pos, int color) {
        var list = new ArrayList<Packet<ClientPlayPacketListener>>();
        var id = VirtualEntityUtils.requestEntityId();
        list.add(new EntitySpawnS2CPacket(id, UUID.randomUUID(),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, EntityType.FIREWORK_ROCKET, 0, Vec3d.ZERO, 0));

        {
            var stack = new ItemStack(Items.FIREWORK_ROCKET);
            stack.getOrCreateNbt().put(FireworkRocketItem.FIREWORKS_KEY, TransportEffects.makeTransportFirework(color));
            list.add(new EntityTrackerUpdateS2CPacket(id, List.of(DataTracker.SerializedEntry.of(FireworkRocketEntityAccessor.getITEM(), stack))));
        }
        {
            var x = PolymerCommonUtils.createUnsafe(EntityStatusS2CPacket.class);
            ((EntityStatusS2CPacketAccessor) x).setId(id);
            ((EntityStatusS2CPacketAccessor) x).setStatus(EntityStatuses.EXPLODE_FIREWORK_CLIENT);
            list.add(x);
        }
        list.add(new EntitiesDestroyS2CPacket(id));

        var b = new BundleS2CPacket(list);

        for (var player : this.player.getWorld().getPlayers()) {
            if (skipOurselves && player == this.player) {
                continue;
            }

            if (player instanceof ServerPlayerEntity serverPlayer && this.player.getPos().isInRange(player.getPos(), 32.0)) {
                serverPlayer.networkHandler.sendPacket(b);
            }
        }
    }

    @Override
    public ElementHolder holder() {
        return this.holder;
    }

    @Override
    public void destroy() {
    }

    @Override
    public Vec3d getPos() {
        return this.player.getPos();
    }

    @Override
    public ServerWorld getWorld() {
        return (ServerWorld) this.player.getWorld();
    }

    @Override
    public void updateCurrentlyTracking(Collection<ServerPlayNetworkHandler> currentlyTracking) {
    }

    @Override
    public void updateTracking(ServerPlayNetworkHandler tracking) {
    }
}
