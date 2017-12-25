package org.sj.iot.model;

/**
 * TCP数据体->消息数据体模型
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-09-06
 */
public class DataBody {
    private String type; // 消息类型
    private GatewayInfo gatewayInfo; // 网关信息
    private Hb hb; // 心跳包
    private Cmd.CmdInfo cmd; // 消息请求命令
    private Cmd.AckInfo ack; // 消息命令应答
    private FirmwareInfo firmwareUpdate; // 固件升级推送信息
    private BackupInfo datafileBackup; // 网关数据文件备份
    private RecoveryInfo datafileRecovery; // 网关数据文件恢复
    private Long utc; // 操作时间
    private String status; // 响应状态

    public DataBody() {
    }

    public DataBody(String type, Long utc, String status) {
        this.type = type;
        this.utc = utc;
        this.status = status;
    }

    public DataBody(GatewayInfo gatewayInfo) {
        this.type = GATEWAY_INFO;
        this.gatewayInfo = gatewayInfo;
    }

    public DataBody(Hb hb) {
        this.type = HB;
        this.hb = hb;
    }

    public DataBody(Cmd.AckInfo ack) {
        this.type = GATEWAY_INFO;
        this.ack = ack;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GatewayInfo getGatewayInfo() {
        return gatewayInfo;
    }

    public void setGatewayInfo(GatewayInfo gatewayInfo) {
        this.gatewayInfo = gatewayInfo;
    }

    public Hb getHb() {
        return hb;
    }

    public void setHb(Hb hb) {
        this.hb = hb;
    }

    public Cmd.CmdInfo getCmd() {
        return cmd;
    }

    public void setCmd(Cmd.CmdInfo cmd) {
        this.cmd = cmd;
    }

    public Cmd.AckInfo getAck() {
        return ack;
    }

    public void setAck(Cmd.AckInfo ack) {
        this.ack = ack;
    }

    public FirmwareInfo getFirmwareUpdate() {
        return firmwareUpdate;
    }

    public void setFirmwareUpdate(FirmwareInfo firmware) {
        this.firmwareUpdate = firmware;
    }

    public BackupInfo getDatafileBackup() {
        return datafileBackup;
    }

    public void setDatafileBackup(BackupInfo backup) {
        this.datafileBackup = backup;
    }

    public RecoveryInfo getDatafileRecovery() {
        return datafileRecovery;
    }

    public void setDatafileRecovery(RecoveryInfo recovery) {
        this.datafileRecovery = recovery;
    }

    public Long getUtc() {
        return utc;
    }

    public void setUtc(Long utc) {
        this.utc = utc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataBody{");
        sb.append("type='").append(type).append('\'');
        if (cmd != null) {
            sb.append(", cmd=").append(cmd);
        }
        if (ack != null) {
            sb.append(", ack=").append(ack);
        }
        if (firmwareUpdate != null) {
            sb.append(", firmwareUpdate=").append(firmwareUpdate);
        }
        if (datafileBackup != null) {
            sb.append(", datafileBackup=").append(datafileBackup);
        }
        if (datafileRecovery != null) {
            sb.append(", datafileRecovery=").append(datafileRecovery);

        }
        if (utc != null) {
            sb.append(", utc=").append(utc);
        }
        if (status != null) {
            sb.append(", status='").append(status).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * 创建心跳数据体
     */
    public static DataBody createCloudMessageByHb(Long totalram, Long freeram, Long totaldisk, Long freedisk) {
        DataBody instance = new DataBody();
        instance.type = HB;
        instance.hb = new Hb(totalram, freeram, totaldisk, freedisk);
        return instance;
    }

    public static final String GATEWAY_INFO = "gateway_info";
    public static final String HB = "hb";
    public static final String REMOTE_CONTROL = "remote_control";
    public static final String FIRMWARE_UPDATE = "firmware_update";
    public static final String DATAFILE_BACKUP = "datafile_backup";
    public static final String DATAFILE_RECOVERY = "datafile_recovery";
    public static final String NEWDEVICES = "newdevices";

    public static class GatewayInfo {
        private String manufacturer;
        private String name;
        private String mac;
        private Integer type;
        private Integer init;
        private Integer mode;
        private String swversion;
        private String hwversion;
        private Integer channel;
        private Integer netid;
        private Integer power;
        private Integer userid;
        private Integer rawuserid;
        private Integer lightcount;
        private Integer radioon;
        private Integer wifimode;
        private String wifissid;
        private String wifipwd;
        private String connssid;
        private String connpwd;
        private Integer autoupdate;
        private Integer autobackup;
        private Integer wgkgilighton;
        private Integer wgkgilightsync;
        private Integer geekmode;
        private String geekkey;
        private Integer remoteenable;
        private String serverip;
        private Integer serverport;
        private Integer mapwidth;
        private Integer mapheight;
        private String userdata;
        private Integer serverconnected;
        private Integer reserve1;
        private Integer reserve2;
        private Integer reserve3;

        public String getManufacturer() {
            return manufacturer;
        }

        public void setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public Integer getInit() {
            return init;
        }

        public void setInit(Integer init) {
            this.init = init;
        }

        public Integer getMode() {
            return mode;
        }

        public void setMode(Integer mode) {
            this.mode = mode;
        }

        public String getSwversion() {
            return swversion;
        }

        public void setSwversion(String swversion) {
            this.swversion = swversion;
        }

        public String getHwversion() {
            return hwversion;
        }

        public void setHwversion(String hwversion) {
            this.hwversion = hwversion;
        }

        public Integer getChannel() {
            return channel;
        }

        public void setChannel(Integer channel) {
            this.channel = channel;
        }

        public Integer getNetid() {
            return netid;
        }

        public void setNetid(Integer netid) {
            this.netid = netid;
        }

        public Integer getPower() {
            return power;
        }

        public void setPower(Integer power) {
            this.power = power;
        }

        public Integer getUserid() {
            return userid;
        }

        public void setUserid(Integer userid) {
            this.userid = userid;
        }

        public Integer getRawuserid() {
            return rawuserid;
        }

        public void setRawuserid(Integer rawuserid) {
            this.rawuserid = rawuserid;
        }

        public Integer getLightcount() {
            return lightcount;
        }

        public void setLightcount(Integer lightcount) {
            this.lightcount = lightcount;
        }

        public Integer getRadioon() {
            return radioon;
        }

        public void setRadioon(Integer radioon) {
            this.radioon = radioon;
        }

        public Integer getWifimode() {
            return wifimode;
        }

        public void setWifimode(Integer wifimode) {
            this.wifimode = wifimode;
        }

        public String getWifissid() {
            return wifissid;
        }

        public void setWifissid(String wifissid) {
            this.wifissid = wifissid;
        }

        public String getWifipwd() {
            return wifipwd;
        }

        public void setWifipwd(String wifipwd) {
            this.wifipwd = wifipwd;
        }

        public String getConnssid() {
            return connssid;
        }

        public void setConnssid(String connssid) {
            this.connssid = connssid;
        }

        public String getConnpwd() {
            return connpwd;
        }

        public void setConnpwd(String connpwd) {
            this.connpwd = connpwd;
        }

        public Integer getAutoupdate() {
            return autoupdate;
        }

        public void setAutoupdate(Integer autoupdate) {
            this.autoupdate = autoupdate;
        }

        public Integer getAutobackup() {
            return autobackup;
        }

        public void setAutobackup(Integer autobackup) {
            this.autobackup = autobackup;
        }

        public Integer getWgkgilighton() {
            return wgkgilighton;
        }

        public void setWgkgilighton(Integer wgkgilighton) {
            this.wgkgilighton = wgkgilighton;
        }

        public Integer getWgkgilightsync() {
            return wgkgilightsync;
        }

        public void setWgkgilightsync(Integer wgkgilightsync) {
            this.wgkgilightsync = wgkgilightsync;
        }

        public Integer getGeekmode() {
            return geekmode;
        }

        public void setGeekmode(Integer geekmode) {
            this.geekmode = geekmode;
        }

        public String getGeekkey() {
            return geekkey;
        }

        public void setGeekkey(String geekkey) {
            this.geekkey = geekkey;
        }

        public Integer getRemoteenable() {
            return remoteenable;
        }

        public void setRemoteenable(Integer remoteenable) {
            this.remoteenable = remoteenable;
        }

        public String getServerip() {
            return serverip;
        }

        public void setServerip(String serverip) {
            this.serverip = serverip;
        }

        public Integer getServerport() {
            return serverport;
        }

        public void setServerport(Integer serverport) {
            this.serverport = serverport;
        }

        public Integer getMapwidth() {
            return mapwidth;
        }

        public void setMapwidth(Integer mapwidth) {
            this.mapwidth = mapwidth;
        }

        public Integer getMapheight() {
            return mapheight;
        }

        public void setMapheight(Integer mapheight) {
            this.mapheight = mapheight;
        }

        public String getUserdata() {
            return userdata;
        }

        public void setUserdata(String userdata) {
            this.userdata = userdata;
        }

        public Integer getServerconnected() {
            return serverconnected;
        }

        public void setServerconnected(Integer serverconnected) {
            this.serverconnected = serverconnected;
        }

        public Integer getReserve1() {
            return reserve1;
        }

        public void setReserve1(Integer reserve1) {
            this.reserve1 = reserve1;
        }

        public Integer getReserve2() {
            return reserve2;
        }

        public void setReserve2(Integer reserve2) {
            this.reserve2 = reserve2;
        }

        public Integer getReserve3() {
            return reserve3;
        }

        public void setReserve3(Integer reserve3) {
            this.reserve3 = reserve3;
        }

        @Override
        public boolean equals(Object that) {
            if (this == that) {
                return true;
            }
            if (that == null) {
                return false;
            }
            if (getClass() != that.getClass()) {
                return false;
            }
            GatewayInfo other = (GatewayInfo) that;
            return this.getMac() == null ? other.getMac() == null : this.getMac().equals(other.getMac());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getManufacturer() == null) ? 0 : getManufacturer().hashCode());
            result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
            result = prime * result + ((getMac() == null) ? 0 : getMac().hashCode());
            result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
            result = prime * result + ((getInit() == null) ? 0 : getInit().hashCode());
            result = prime * result + ((getMode() == null) ? 0 : getMode().hashCode());
            result = prime * result + ((getSwversion() == null) ? 0 : getSwversion().hashCode());
            result = prime * result + ((getHwversion() == null) ? 0 : getHwversion().hashCode());
            result = prime * result + ((getChannel() == null) ? 0 : getChannel().hashCode());
            result = prime * result + ((getNetid() == null) ? 0 : getNetid().hashCode());
            result = prime * result + ((getPower() == null) ? 0 : getPower().hashCode());
            result = prime * result + ((getUserid() == null) ? 0 : getUserid().hashCode());
            result = prime * result + ((getRawuserid() == null) ? 0 : getRawuserid().hashCode());
            result = prime * result + ((getLightcount() == null) ? 0 : getLightcount().hashCode());
            result = prime * result + ((getRadioon() == null) ? 0 : getRadioon().hashCode());
            result = prime * result + ((getWifimode() == null) ? 0 : getWifimode().hashCode());
            result = prime * result + ((getWifissid() == null) ? 0 : getWifissid().hashCode());
            result = prime * result + ((getWifipwd() == null) ? 0 : getWifipwd().hashCode());
            result = prime * result + ((getConnssid() == null) ? 0 : getConnssid().hashCode());
            result = prime * result + ((getConnpwd() == null) ? 0 : getConnpwd().hashCode());
            result = prime * result + ((getAutoupdate() == null) ? 0 : getAutoupdate().hashCode());
            result = prime * result + ((getAutobackup() == null) ? 0 : getAutobackup().hashCode());
            result = prime * result + ((getWgkgilighton() == null) ? 0 : getWgkgilighton().hashCode());
            result = prime * result + ((getWgkgilightsync() == null) ? 0 : getWgkgilightsync().hashCode());
            result = prime * result + ((getGeekmode() == null) ? 0 : getGeekmode().hashCode());
            result = prime * result + ((getGeekkey() == null) ? 0 : getGeekkey().hashCode());
            result = prime * result + ((getRemoteenable() == null) ? 0 : getRemoteenable().hashCode());
            result = prime * result + ((getServerip() == null) ? 0 : getServerip().hashCode());
            result = prime * result + ((getServerport() == null) ? 0 : getServerport().hashCode());
            result = prime * result + ((getMapwidth() == null) ? 0 : getMapwidth().hashCode());
            result = prime * result + ((getMapheight() == null) ? 0 : getMapheight().hashCode());
            result = prime * result + ((getUserdata() == null) ? 0 : getUserdata().hashCode());
            result = prime * result + ((getServerconnected() == null) ? 0 : getServerconnected().hashCode());
            result = prime * result + ((getReserve1() == null) ? 0 : getReserve1().hashCode());
            result = prime * result + ((getReserve2() == null) ? 0 : getReserve2().hashCode());
            result = prime * result + ((getReserve3() == null) ? 0 : getReserve3().hashCode());
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName());
            sb.append(" [");
            sb.append("Hash = ").append(hashCode());
            sb.append(", manufacturer=").append(manufacturer);
            sb.append(", name=").append(name);
            sb.append(", mac=").append(mac);
            sb.append(", type=").append(type);
            sb.append(", init=").append(init);
            sb.append(", mode=").append(mode);
            sb.append(", swversion=").append(swversion);
            sb.append(", hwversion=").append(hwversion);
            sb.append(", channel=").append(channel);
            sb.append(", netid=").append(netid);
            sb.append(", power=").append(power);
            sb.append(", userid=").append(userid);
            sb.append(", rawuserid=").append(rawuserid);
            sb.append(", lightcount=").append(lightcount);
            sb.append(", radioon=").append(radioon);
            sb.append(", wifimode=").append(wifimode);
            sb.append(", wifissid=").append(wifissid);
            sb.append(", wifipwd=").append(wifipwd);
            sb.append(", connssid=").append(connssid);
            sb.append(", connpwd=").append(connpwd);
            sb.append(", autoupdate=").append(autoupdate);
            sb.append(", autobackup=").append(autobackup);
            sb.append(", wgkgilighton=").append(wgkgilighton);
            sb.append(", wgkgilightsync=").append(wgkgilightsync);
            sb.append(", geekmode=").append(geekmode);
            sb.append(", geekkey=").append(geekkey);
            sb.append(", remoteenable=").append(remoteenable);
            sb.append(", serverip=").append(serverip);
            sb.append(", serverport=").append(serverport);
            sb.append(", mapwidth=").append(mapwidth);
            sb.append(", mapheight=").append(mapheight);
            sb.append(", userdata=").append(userdata);
            sb.append(", serverconnected=").append(serverconnected);
            sb.append(", reserve1=").append(reserve1);
            sb.append(", reserve2=").append(reserve2);
            sb.append(", reserve3=").append(reserve3);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class Hb {
        private Long totalram; // 网关设备总内存
        private Long freeram; // 网关设备空闲内存
        private Long totaldisk; // 网关设备总磁盘大小
        private Long freedisk; // 网关设备空闲磁盘大小

        public Hb() {
        }

        public Hb(Long totalram, Long freeram, Long totaldisk, Long freedisk) {
            this.totalram = totalram;
            this.freeram = freeram;
            this.totaldisk = totaldisk;
            this.freedisk = freedisk;
        }

        public Long getTotalram() {
            return totalram;
        }

        public void setTotalram(Long totalram) {
            this.totalram = totalram;
        }

        public Long getFreeram() {
            return freeram;
        }

        public void setFreeram(Long freeram) {
            this.freeram = freeram;
        }

        public Long getTotaldisk() {
            return totaldisk;
        }

        public void setTotaldisk(Long totaldisk) {
            this.totaldisk = totaldisk;
        }

        public Long getFreedisk() {
            return freedisk;
        }

        public void setFreedisk(Long freedisk) {
            this.freedisk = freedisk;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Hb{");
            sb.append("totalram=").append(totalram);
            sb.append(", freeram=").append(freeram);
            sb.append(", totaldisk=").append(totaldisk);
            sb.append(", freedisk=").append(freedisk);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * 扩展接口标识
     */
    interface Ext {
    }

    public static final class BackupInfo implements Ext {
        private String backupUrl;

        public String getBackupUrl() {
            return backupUrl;
        }

        public void setBackupUrl(String backupUrl) {
            this.backupUrl = backupUrl;
        }
    }

    public static final class RecoveryInfo implements Ext {
        private String recoveryUrl;
        private String recoveryMd5;

        public String getRecoveryUrl() {
            return recoveryUrl;
        }

        public void setRecoveryUrl(String recoveryUrl) {
            this.recoveryUrl = recoveryUrl;
        }

        public String getRecoveryMd5() {
            return recoveryMd5;
        }

        public void setRecoveryMd5(String recoveryMd5) {
            this.recoveryMd5 = recoveryMd5;
        }
    }

    public static final class FirmwareInfo implements Ext {
        private String firmwareUrl; // 固件包地址
        private String firmwareMd5; // 固件包MD5
        private String version; // 固件版本

        public String getFirmwareUrl() {
            return firmwareUrl;
        }

        public void setFirmwareUrl(String firmwareUrl) {
            this.firmwareUrl = firmwareUrl;
        }

        public String getFirmwareMd5() {
            return firmwareMd5;
        }

        public void setFirmwareMd5(String firmwareMd5) {
            this.firmwareMd5 = firmwareMd5;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}