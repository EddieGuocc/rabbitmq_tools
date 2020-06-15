# rabbitmq_tools
using for normal rabbitmq operation with java






## Ubuntu Server配置固定ip ##

原理：NAT模式下为物理机和虚拟机配置固定ip地址，组成内网，可以随意设定ip地址，不受公网ip影响。

虚拟机版本： Ubuntu Server 18.04

VMWare版本： 15Pro

### 配置VMWare虚拟网络 ###
1. 首先查看VMWare安装是否正常，物理机网络适配器是否有VMnet1 和 VMnet8 两个虚拟网卡。
2. 菜单栏 - 编辑 - 虚拟网络编辑器 - VMnet8
因为VMnet8默认就是NAT模式的虚拟网卡，所以只修改【子网IP】和【子网掩码】即可，
子网掩码根据自定义的子网IP的地址类型进行配置。
3. VMnet8-NAT设置
网关地址不要和上一步子网IP重复，网段要一致，一般最后一位是2。
4. VMnet8-DHCP设置
设置分配地址区间,避开网关地址。
5. 保存

### 设置物理机地址 ###
找到网络适配器，将VMnet8的IP地址设置成与虚拟网络相同的网段，最后一位不要和前一步的地址范围/网关重复。

### 虚拟机netplan网络设置 ###
主要集中在/etc/netplan/XXXX.yaml文件夹下，配置如下

	network:
	    ethernets:
	        ens33:
	            addresses: [10.0.3.25/24] #在地址段内随意分配固定ip，最后的地址类型不要忘记填写
	            dhcp4: no
	            dhcp6: no
	            gateway4: 10.0.3.2 #网关地址与之前设置的一致
	            nameservers: 
	                addresses: [114.114.114.114, 114.114.115.115] #配置DNS 不然无法访问互联网
	    version: 2
	    renderer: networkd

配置完成后使其生效 输入 netplan apply

测试 ping 互联网是否能通 ping 物理机是否能通

查看ssh的状态 service ssh status

查看防火墙状态ufw status

可以远程使用root用户登陆 修改/etc/ssh下文件 sshd_config 将PermitRootLogin打开 后面更改为yes

重启ssh /etc/init.d/ssh restart
