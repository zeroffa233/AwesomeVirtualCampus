-- MySQL dump 10.13  Distrib 8.4.6, for Win64 (x86_64)
--
-- Host: localhost    Database: vcampus
-- ------------------------------------------------------
-- Server version	8.4.6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book` (
  `uuid` binary(16) NOT NULL,
  `author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `bookStatus` enum('available','lend','newly','returned') COLLATE utf8mb4_unicode_ci NOT NULL,
  `call_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `isbn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `press` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book`
--

LOCK TABLES `book` WRITE;
/*!40000 ALTER TABLE `book` DISABLE KEYS */;
INSERT INTO `book` VALUES (_binary '	Qa锟絀锟\�','浣欏崕','lend','I247.5993','http://img3m4.ddimg.cn/6/32/663823914-1_w_3.jpg','銆婃椿鐫 銆嬭杩颁簡鍦ㄥぇ鏃朵唬鑳屾櫙涓嬶紝闅忕潃鍐呮垬銆佷笁鍙嶄簲鍙嶃 佸ぇ璺冭繘銆佲 滄枃鍖栧ぇ闈╁懡鈥濈瓑绀句細鍙橀潻锛屽緪绂忚吹鐨勪汉鐢熷拰瀹跺涵涓嶆柇缁忓彈鐫 鑻﹂毦锛屽埌浜嗘渶鍚庢墍鏈変翰浜洪兘鍏堝悗绂讳粬鑰屽幓锛屼粎鍓╀笅骞磋 佺殑浠栧拰涓 澶磋 佺墰鐩镐緷涓哄懡銆傚皬璇翠互鏅 氥 佸钩瀹炵殑鏁呬簨鎯呰妭璁茶堪浜嗗湪鎬ュ墽鍙橀潻鐨勬椂浠ｄ腑绂忚吹鐨勪笉骞搁伃閬囧拰鍧庡澐鍛借繍锛屽湪鍐烽潤鐨勭瑪瑙︿腑灞曠幇浜嗙敓鍛界殑鎰忎箟鍜屽瓨鍦ㄧ殑浠峰 硷紝鎻ず浜嗗懡杩愮殑鏃犲锛屼笌鐢熸椿鐨勪笉鍙崏鎽搞  ','9787506365390','娲荤潃','鏉庢枃姝ｅ浘涔﹂涓枃鍥句功闃呰瀹 ','浣滃鍑虹増绀 '),(_binary '>锟 EWF锟 $I','鍒樼憸','newly','D0/639','https://i.dawnlab.me/935400df71a34577a43e116bc6c361e6.jpg','涓 涓拰骞斥 滅垎鍙戔 濈殑骞翠唬锛  鍘嗗彶鈥滅粓缁撹鈥濈殑缁堢粨锛  鍥藉鑳藉姏浠庝綍鑰屾潵锛  鏂囨槑鐨勫啿绐佹槸涓 涓繃鏃剁殑棰勮█锛  鈥︹   闈㈠鏋楁灄鎬绘 荤殑鏀挎不闂锛屼綔鑰呭甫棰嗘垜浠互涓 绉嶆瘮杈冪殑瑙嗚锛屽湪姘戜富闂矗鍜屽浗瀹惰兘鍔涗袱涓斂娌绘瘮杈冪殑鏍稿績缁村害涓嬶紝寤虹珛璧疯瀵熺殑鍙傜収绯伙紝灏嗕笉鍚屼綋鍒躲 佷笉鍚岀粡娴庡彂灞曟按骞崇殑鍥藉绾冲叆姣旇緝鐨勮閲庯紝鍘诲垎鏋愭垜浠殑鏃朵唬鑳屾櫙鍜屽叏鐞冨寲杩涚▼锛岃璁轰笉鍚屽浗瀹剁殑鏀挎不杞瀷涓庡浗瀹惰兘鍔涳紝浠ュ強鏂囧寲鍜岀粡娴庡鏀挎不鍙樿縼鐨勫奖鍝嶃   鈥滄斂娌绘槸鍙兘鎬х殑鑹烘湳銆傗 濆綋鎴戜滑灏嗛潰瀵圭殑鏀挎不鐜板疄褰撲綔涓 涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屽氨鑳戒粠姝ゆ椂姝ゅ湴鎶界锛岃幏寰椾竴绉嶄刊鐬扮殑瑙嗚锛岃繘鑰屽啀鑱氱劍瀹氫綅鐜板疄锛屽湪娴╃ 氱殑鍙兘鎬т腑鐞嗚В鎴戜滑鑷韩銆  銆屽悕浜烘帹鑽愩   姣旇緝鏀挎不瀛﹀綋涓殑鈥滄瘮杈冣 濓紝涓庡叾璇存槸涓 绉嶅叿浣撶殑鐮旂┒鏂规硶锛屼笉濡傝鏄竴绉嶇爺绌剁殑瑙嗛噹銆傚綋浣犳妸浣犳墍闈㈠鐨勬斂娌荤幇瀹炲綋浣滀竴涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屼綘灏辫幏寰椾簡涓 绉嶁 滄瘮杈冪殑瑙嗛噹鈥濄   姣旇緝鐨勮閲庢湰璐ㄤ笂鏄竴绉 ... (灞曞紑鍏ㄩ儴) 銆屽唴瀹圭畝浠嬨   涓 涓拰骞斥 滅垎鍙戔 濈殑骞翠唬锛  鍘嗗彶鈥滅粓缁撹鈥濈殑缁堢粨锛  鍥藉鑳藉姏浠庝綍鑰屾潵锛  鏂囨槑鐨勫啿绐佹槸涓 涓繃鏃剁殑棰勮█锛  鈥︹   闈㈠鏋楁灄鎬绘 荤殑鏀挎不闂锛屼綔鑰呭甫棰嗘垜浠互涓 绉嶆瘮杈冪殑瑙嗚锛屽湪姘戜富闂矗鍜屽浗瀹惰兘鍔涗袱涓斂娌绘瘮杈冪殑鏍稿績缁村害涓嬶紝寤虹珛璧疯瀵熺殑鍙傜収绯伙紝灏嗕笉鍚屼綋鍒躲 佷笉鍚岀粡娴庡彂灞曟按骞崇殑鍥藉绾冲叆姣旇緝鐨勮閲庯紝鍘诲垎鏋愭垜浠殑鏃朵唬鑳屾櫙鍜屽叏鐞冨寲杩涚▼锛岃璁轰笉鍚屽浗瀹剁殑鏀挎不杞瀷涓庡浗瀹惰兘鍔涳紝浠ュ強鏂囧寲鍜岀粡娴庡鏀挎不鍙樿縼鐨勫奖鍝嶃   鈥滄斂娌绘槸鍙兘鎬х殑鑹烘湳銆傗 濆綋鎴戜滑灏嗛潰瀵圭殑鏀挎不鐜板疄褰撲綔涓 涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屽氨鑳戒粠姝ゆ椂姝ゅ湴鎶界锛岃幏寰椾竴绉嶄刊鐬扮殑瑙嗚锛岃繘鑰屽啀鑱氱劍瀹氫綅鐜板疄锛屽湪娴╃ 氱殑鍙兘鎬т腑鐞嗚В鎴戜滑鑷韩銆 ','9787559848048','鍙兘鎬х殑鑹烘湳锛氭瘮杈冩斂娌诲30璁 ','涓枃鍥句功闃呰瀹 1锛堜節榫欐箹A208锛 ','骞胯タ甯堣寖澶у鍑虹増绀 '),(_binary 'OqGbHl锟 1d\�','鍖楀矝','available','I217.02/1471','https://i.dawnlab.me/e23c468d53e7c4fa5846bd2fac402ae1.png','銆婂繀鏈変汉閲嶅啓鐖辨儏銆嬫槸钁楀悕璇椾汉鍖楀矝鐨勪綔鍝佺簿閫夐泦锛 46绡囩粡鍏歌瘲姝屼笌鏁ｆ枃浣滃搧锛屽憟鐜拌瘲浜恒 佹暎鏂囧銆佺敾鑰呫 佹憚褰辫 呭寳宀涚殑鍒涗綔鍏ㄨ矊銆傗 滀竴璇椾竴鏂団 濈殑缂栨帓鐗硅壊锛屽鍚屼腑鍥借瘲璇濅紶缁熶箣浼犵画銆備粠鈥滄毚椋庨洦鐨勮蹇嗏 濆埌鈥滆瘝鐨勬祦浜♀ 濆埌鈥滃ぇ鍦颁箣涔︹ 濓紝涓夎緫璇楁枃浣滃搧瀹屾暣灞曠幇璇椾汉鑷骞磋嚦浠婄殑浜虹敓琛屾梾锛屾槧鐓т簩鍗佷笘绾笅鍗婂彾婵 鑽＄殑鏃朵唬椋庢櫙銆  鈥滄垜鍙楅泧浜庝竴涓紵澶х殑璁板繂銆傗 濆寳宀涚殑璇楁瓕鏄竴浠ｄ汉鐨勯泦浣撹蹇嗕笌绮剧闀滃儚銆傝嚜鎴戜笌鏃朵唬锛屼粬涔′笌鏁呬埂锛屽巻鍙蹭笌鐜板疄锛屽湪绉嶇鎮栬涓庢柇瑁備腑锛屽湪鍑哄彂涓庢姷杈句箣闂达紝鍖楀矝鎶婅瘝璇瀿杩涘巻鍙层 傚寳宀涚殑鏁ｆ枃鍒欑畝鍑 鏈夊瓒ｏ紝浠庤壘浼︹ ㈤噾鏂牎鐨勭航绾﹀埌鍗″か鍗＄殑甯冩媺鏍硷紝浠庨樋鎷夋硶鐗圭殑鎷夐┈鎷夊埌娲涘皵杩︾殑瑗跨彮鐗欙紝浠栧湪婕傛硦鐨勪笘鐣岃鏃呬腑涓庤韩浠藉悇寮傜殑璇椾汉瀛﹁ 呯瓑鐩搁 ㈢浉闄咃紝鎵 瑙佹墍闂婚矞娲讳害鎯婂績锛涗篃蹇嗗皯骞村線浜嬨 佷翰鍙嬫寶浜わ紝娴撶儓涔℃剚涓庡皷閿愮柤鐥涘父涓嶆湡鑰岃嚦銆傝瘲浜轰箣绗斿啓浣滄暎鏂囷紝鎭㈠姹夎鐨勪赴瀵屻 佹晱閿愩 佹柊椴溿 備功涓害鏈夊骞呭寳宀涚殑缁 ... (灞曞紑鍏ㄩ儴) 鍐呭绠 浠嬶細 銆婂繀鏈変汉閲嶅啓鐖辨儏銆嬫槸钁楀悕璇椾汉鍖楀矝鐨勪綔鍝佺簿閫夐泦锛 46绡囩粡鍏歌瘲姝屼笌鏁ｆ枃浣滃搧锛屽憟鐜拌瘲浜恒 佹暎鏂囧銆佺敾鑰呫 佹憚褰辫 呭寳宀涚殑鍒涗綔鍏ㄨ矊銆傗 滀竴璇椾竴鏂団 濈殑缂栨帓鐗硅壊锛屽鍚屼腑鍥借瘲璇濅紶缁熶箣浼犵画銆備粠鈥滄毚椋庨洦鐨勮蹇嗏 濆埌鈥滆瘝鐨勬祦浜♀ 濆埌鈥滃ぇ鍦颁箣涔︹ 濓紝涓夎緫璇楁枃浣滃搧瀹屾暣灞曠幇璇椾汉鑷骞磋嚦浠婄殑浜虹敓琛屾梾锛屾槧鐓т簩鍗佷笘绾笅鍗婂彾婵 鑽＄殑鏃朵唬椋庢櫙銆  鈥滄垜鍙楅泧浜庝竴涓紵澶х殑璁板繂銆傗 濆寳宀涚殑璇楁瓕鏄竴浠ｄ汉鐨勯泦浣撹蹇嗕笌绮剧闀滃儚銆傝嚜鎴戜笌鏃朵唬锛屼粬涔′笌鏁呬埂锛屽巻鍙蹭笌鐜板疄锛屽湪绉嶇鎮栬涓庢柇瑁備腑锛屽湪鍑哄彂涓庢姷杈句箣闂达紝鍖楀矝鎶婅瘝璇瀿杩涘巻鍙层 傚寳宀涚殑鏁ｆ枃鍒欑畝鍑 鏈夊瓒ｏ紝浠庤壘浼︹ ㈤噾鏂牎鐨勭航绾﹀埌鍗″か鍗＄殑甯冩媺鏍硷紝浠庨樋鎷夋硶鐗圭殑鎷夐┈鎷夊埌娲涘皵杩︾殑瑗跨彮鐗欙紝浠栧湪婕傛硦鐨勪笘鐣岃鏃呬腑涓庤韩浠藉悇寮傜殑璇椾汉瀛﹁ 呯瓑鐩搁 ㈢浉闄咃紝鎵 瑙佹墍闂婚矞娲讳害鎯婂績锛涗篃蹇嗗皯骞村線浜嬨 佷翰鍙嬫寶浜わ紝娴撶儓涔℃剚涓庡皷閿愮柤鐥涘父涓嶆湡鑰岃嚦銆傝瘲浜轰箣绗斿啓浣滄暎鏂囷紝鎭㈠姹夎鐨勪赴瀵屻 佹晱閿愩 佹柊椴溿 備功涓害鏈夊骞呭寳宀涚殑缁樼敾涓庢憚褰变綔鍝侊紝杩欐槸浠栧湪鏂囧瓧涔嬪鎵惧鍒扮殑鍙︿竴绉嶈瑷 銆 ','9787544398565','蹇呮湁浜洪噸鍐欑埍鎯 ','涓枃鍥句功闃呰瀹 1锛堜節榫欐箹A208锛 ','娴峰崡鍑虹増绀 '),(_binary ' 锟斤拷锟 \'','鍒樼憸','available','D0/639','https://i.dawnlab.me/935400df71a34577a43e116bc6c361e6.jpg','涓 涓拰骞斥 滅垎鍙戔 濈殑骞翠唬锛  鍘嗗彶鈥滅粓缁撹鈥濈殑缁堢粨锛  鍥藉鑳藉姏浠庝綍鑰屾潵锛  鏂囨槑鐨勫啿绐佹槸涓 涓繃鏃剁殑棰勮█锛  鈥︹   闈㈠鏋楁灄鎬绘 荤殑鏀挎不闂锛屼綔鑰呭甫棰嗘垜浠互涓 绉嶆瘮杈冪殑瑙嗚锛屽湪姘戜富闂矗鍜屽浗瀹惰兘鍔涗袱涓斂娌绘瘮杈冪殑鏍稿績缁村害涓嬶紝寤虹珛璧疯瀵熺殑鍙傜収绯伙紝灏嗕笉鍚屼綋鍒躲 佷笉鍚岀粡娴庡彂灞曟按骞崇殑鍥藉绾冲叆姣旇緝鐨勮閲庯紝鍘诲垎鏋愭垜浠殑鏃朵唬鑳屾櫙鍜屽叏鐞冨寲杩涚▼锛岃璁轰笉鍚屽浗瀹剁殑鏀挎不杞瀷涓庡浗瀹惰兘鍔涳紝浠ュ強鏂囧寲鍜岀粡娴庡鏀挎不鍙樿縼鐨勫奖鍝嶃   鈥滄斂娌绘槸鍙兘鎬х殑鑹烘湳銆傗 濆綋鎴戜滑灏嗛潰瀵圭殑鏀挎不鐜板疄褰撲綔涓 涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屽氨鑳戒粠姝ゆ椂姝ゅ湴鎶界锛岃幏寰椾竴绉嶄刊鐬扮殑瑙嗚锛岃繘鑰屽啀鑱氱劍瀹氫綅鐜板疄锛屽湪娴╃ 氱殑鍙兘鎬т腑鐞嗚В鎴戜滑鑷韩銆  銆屽悕浜烘帹鑽愩   姣旇緝鏀挎不瀛﹀綋涓殑鈥滄瘮杈冣 濓紝涓庡叾璇存槸涓 绉嶅叿浣撶殑鐮旂┒鏂规硶锛屼笉濡傝鏄竴绉嶇爺绌剁殑瑙嗛噹銆傚綋浣犳妸浣犳墍闈㈠鐨勬斂娌荤幇瀹炲綋浣滀竴涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屼綘灏辫幏寰椾簡涓 绉嶁 滄瘮杈冪殑瑙嗛噹鈥濄   姣旇緝鐨勮閲庢湰璐ㄤ笂鏄竴绉 ... (灞曞紑鍏ㄩ儴) 銆屽唴瀹圭畝浠嬨   涓 涓拰骞斥 滅垎鍙戔 濈殑骞翠唬锛  鍘嗗彶鈥滅粓缁撹鈥濈殑缁堢粨锛  鍥藉鑳藉姏浠庝綍鑰屾潵锛  鏂囨槑鐨勫啿绐佹槸涓 涓繃鏃剁殑棰勮█锛  鈥︹   闈㈠鏋楁灄鎬绘 荤殑鏀挎不闂锛屼綔鑰呭甫棰嗘垜浠互涓 绉嶆瘮杈冪殑瑙嗚锛屽湪姘戜富闂矗鍜屽浗瀹惰兘鍔涗袱涓斂娌绘瘮杈冪殑鏍稿績缁村害涓嬶紝寤虹珛璧疯瀵熺殑鍙傜収绯伙紝灏嗕笉鍚屼綋鍒躲 佷笉鍚岀粡娴庡彂灞曟按骞崇殑鍥藉绾冲叆姣旇緝鐨勮閲庯紝鍘诲垎鏋愭垜浠殑鏃朵唬鑳屾櫙鍜屽叏鐞冨寲杩涚▼锛岃璁轰笉鍚屽浗瀹剁殑鏀挎不杞瀷涓庡浗瀹惰兘鍔涳紝浠ュ強鏂囧寲鍜岀粡娴庡鏀挎不鍙樿縼鐨勫奖鍝嶃   鈥滄斂娌绘槸鍙兘鎬х殑鑹烘湳銆傗 濆綋鎴戜滑灏嗛潰瀵圭殑鏀挎不鐜板疄褰撲綔涓 涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屽氨鑳戒粠姝ゆ椂姝ゅ湴鎶界锛岃幏寰椾竴绉嶄刊鐬扮殑瑙嗚锛岃繘鑰屽啀鑱氱劍瀹氫綅鐜板疄锛屽湪娴╃ 氱殑鍙兘鎬т腑鐞嗚В鎴戜滑鑷韩銆 ','9787559848048','鍙兘鎬х殑鑹烘湳锛氭瘮杈冩斂娌诲30璁 ','涓枃鍥句功闃呰瀹 1锛堜節榫欐箹A208锛 ','骞胯タ甯堣寖澶у鍑虹増绀 '),(_binary '7f锟絨\0%N锟\�','鍒樼憸','lend','D0/639','https://i.dawnlab.me/935400df71a34577a43e116bc6c361e6.jpg','涓 涓拰骞斥 滅垎鍙戔 濈殑骞翠唬锛  鍘嗗彶鈥滅粓缁撹鈥濈殑缁堢粨锛  鍥藉鑳藉姏浠庝綍鑰屾潵锛  鏂囨槑鐨勫啿绐佹槸涓 涓繃鏃剁殑棰勮█锛  鈥︹   闈㈠鏋楁灄鎬绘 荤殑鏀挎不闂锛屼綔鑰呭甫棰嗘垜浠互涓 绉嶆瘮杈冪殑瑙嗚锛屽湪姘戜富闂矗鍜屽浗瀹惰兘鍔涗袱涓斂娌绘瘮杈冪殑鏍稿績缁村害涓嬶紝寤虹珛璧疯瀵熺殑鍙傜収绯伙紝灏嗕笉鍚屼綋鍒躲 佷笉鍚岀粡娴庡彂灞曟按骞崇殑鍥藉绾冲叆姣旇緝鐨勮閲庯紝鍘诲垎鏋愭垜浠殑鏃朵唬鑳屾櫙鍜屽叏鐞冨寲杩涚▼锛岃璁轰笉鍚屽浗瀹剁殑鏀挎不杞瀷涓庡浗瀹惰兘鍔涳紝浠ュ強鏂囧寲鍜岀粡娴庡鏀挎不鍙樿縼鐨勫奖鍝嶃   鈥滄斂娌绘槸鍙兘鎬х殑鑹烘湳銆傗 濆綋鎴戜滑灏嗛潰瀵圭殑鏀挎不鐜板疄褰撲綔涓 涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屽氨鑳戒粠姝ゆ椂姝ゅ湴鎶界锛岃幏寰椾竴绉嶄刊鐬扮殑瑙嗚锛岃繘鑰屽啀鑱氱劍瀹氫綅鐜板疄锛屽湪娴╃ 氱殑鍙兘鎬т腑鐞嗚В鎴戜滑鑷韩銆  銆屽悕浜烘帹鑽愩   姣旇緝鏀挎不瀛﹀綋涓殑鈥滄瘮杈冣 濓紝涓庡叾璇存槸涓 绉嶅叿浣撶殑鐮旂┒鏂规硶锛屼笉濡傝鏄竴绉嶇爺绌剁殑瑙嗛噹銆傚綋浣犳妸浣犳墍闈㈠鐨勬斂娌荤幇瀹炲綋浣滀竴涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屼綘灏辫幏寰椾簡涓 绉嶁 滄瘮杈冪殑瑙嗛噹鈥濄   姣旇緝鐨勮閲庢湰璐ㄤ笂鏄竴绉 ... (灞曞紑鍏ㄩ儴) 銆屽唴瀹圭畝浠嬨   涓 涓拰骞斥 滅垎鍙戔 濈殑骞翠唬锛  鍘嗗彶鈥滅粓缁撹鈥濈殑缁堢粨锛  鍥藉鑳藉姏浠庝綍鑰屾潵锛  鏂囨槑鐨勫啿绐佹槸涓 涓繃鏃剁殑棰勮█锛  鈥︹   闈㈠鏋楁灄鎬绘 荤殑鏀挎不闂锛屼綔鑰呭甫棰嗘垜浠互涓 绉嶆瘮杈冪殑瑙嗚锛屽湪姘戜富闂矗鍜屽浗瀹惰兘鍔涗袱涓斂娌绘瘮杈冪殑鏍稿績缁村害涓嬶紝寤虹珛璧疯瀵熺殑鍙傜収绯伙紝灏嗕笉鍚屼綋鍒躲 佷笉鍚岀粡娴庡彂灞曟按骞崇殑鍥藉绾冲叆姣旇緝鐨勮閲庯紝鍘诲垎鏋愭垜浠殑鏃朵唬鑳屾櫙鍜屽叏鐞冨寲杩涚▼锛岃璁轰笉鍚屽浗瀹剁殑鏀挎不杞瀷涓庡浗瀹惰兘鍔涳紝浠ュ強鏂囧寲鍜岀粡娴庡鏀挎不鍙樿縼鐨勫奖鍝嶃   鈥滄斂娌绘槸鍙兘鎬х殑鑹烘湳銆傗 濆綋鎴戜滑灏嗛潰瀵圭殑鏀挎不鐜板疄褰撲綔涓 涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屽氨鑳戒粠姝ゆ椂姝ゅ湴鎶界锛岃幏寰椾竴绉嶄刊鐬扮殑瑙嗚锛岃繘鑰屽啀鑱氱劍瀹氫綅鐜板疄锛屽湪娴╃ 氱殑鍙兘鎬т腑鐞嗚В鎴戜滑鑷韩銆 ','9787559848048','鍙兘鎬х殑鑹烘湳锛氭瘮杈冩斂娌诲30璁 ','涓枃鍥句功闃呰瀹 1锛堜節榫欐箹A208锛 ','骞胯タ甯堣寖澶у鍑虹増绀 '),(_binary ';鞚诧拷锟紷','鍒樼憸','newly','D0/639','https://i.dawnlab.me/935400df71a34577a43e116bc6c361e6.jpg','涓 涓拰骞斥 滅垎鍙戔 濈殑骞翠唬锛  鍘嗗彶鈥滅粓缁撹鈥濈殑缁堢粨锛  鍥藉鑳藉姏浠庝綍鑰屾潵锛  鏂囨槑鐨勫啿绐佹槸涓 涓繃鏃剁殑棰勮█锛  鈥︹   闈㈠鏋楁灄鎬绘 荤殑鏀挎不闂锛屼綔鑰呭甫棰嗘垜浠互涓 绉嶆瘮杈冪殑瑙嗚锛屽湪姘戜富闂矗鍜屽浗瀹惰兘鍔涗袱涓斂娌绘瘮杈冪殑鏍稿績缁村害涓嬶紝寤虹珛璧疯瀵熺殑鍙傜収绯伙紝灏嗕笉鍚屼綋鍒躲 佷笉鍚岀粡娴庡彂灞曟按骞崇殑鍥藉绾冲叆姣旇緝鐨勮閲庯紝鍘诲垎鏋愭垜浠殑鏃朵唬鑳屾櫙鍜屽叏鐞冨寲杩涚▼锛岃璁轰笉鍚屽浗瀹剁殑鏀挎不杞瀷涓庡浗瀹惰兘鍔涳紝浠ュ強鏂囧寲鍜岀粡娴庡鏀挎不鍙樿縼鐨勫奖鍝嶃   鈥滄斂娌绘槸鍙兘鎬х殑鑹烘湳銆傗 濆綋鎴戜滑灏嗛潰瀵圭殑鏀挎不鐜板疄褰撲綔涓 涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屽氨鑳戒粠姝ゆ椂姝ゅ湴鎶界锛岃幏寰椾竴绉嶄刊鐬扮殑瑙嗚锛岃繘鑰屽啀鑱氱劍瀹氫綅鐜板疄锛屽湪娴╃ 氱殑鍙兘鎬т腑鐞嗚В鎴戜滑鑷韩銆  銆屽悕浜烘帹鑽愩   姣旇緝鏀挎不瀛﹀綋涓殑鈥滄瘮杈冣 濓紝涓庡叾璇存槸涓 绉嶅叿浣撶殑鐮旂┒鏂规硶锛屼笉濡傝鏄竴绉嶇爺绌剁殑瑙嗛噹銆傚綋浣犳妸浣犳墍闈㈠鐨勬斂娌荤幇瀹炲綋浣滀竴涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屼綘灏辫幏寰椾簡涓 绉嶁 滄瘮杈冪殑瑙嗛噹鈥濄   姣旇緝鐨勮閲庢湰璐ㄤ笂鏄竴绉 ... (灞曞紑鍏ㄩ儴) 銆屽唴瀹圭畝浠嬨   涓 涓拰骞斥 滅垎鍙戔 濈殑骞翠唬锛  鍘嗗彶鈥滅粓缁撹鈥濈殑缁堢粨锛  鍥藉鑳藉姏浠庝綍鑰屾潵锛  鏂囨槑鐨勫啿绐佹槸涓 涓繃鏃剁殑棰勮█锛  鈥︹   闈㈠鏋楁灄鎬绘 荤殑鏀挎不闂锛屼綔鑰呭甫棰嗘垜浠互涓 绉嶆瘮杈冪殑瑙嗚锛屽湪姘戜富闂矗鍜屽浗瀹惰兘鍔涗袱涓斂娌绘瘮杈冪殑鏍稿績缁村害涓嬶紝寤虹珛璧疯瀵熺殑鍙傜収绯伙紝灏嗕笉鍚屼綋鍒躲 佷笉鍚岀粡娴庡彂灞曟按骞崇殑鍥藉绾冲叆姣旇緝鐨勮閲庯紝鍘诲垎鏋愭垜浠殑鏃朵唬鑳屾櫙鍜屽叏鐞冨寲杩涚▼锛岃璁轰笉鍚屽浗瀹剁殑鏀挎不杞瀷涓庡浗瀹惰兘鍔涳紝浠ュ強鏂囧寲鍜岀粡娴庡鏀挎不鍙樿縼鐨勫奖鍝嶃   鈥滄斂娌绘槸鍙兘鎬х殑鑹烘湳銆傗 濆綋鎴戜滑灏嗛潰瀵圭殑鏀挎不鐜板疄褰撲綔涓 涓囩鍙兘鎬т箣涓 鏉ュ寰呮椂锛屽氨鑳戒粠姝ゆ椂姝ゅ湴鎶界锛岃幏寰椾竴绉嶄刊鐬扮殑瑙嗚锛岃繘鑰屽啀鑱氱劍瀹氫綅鐜板疄锛屽湪娴╃ 氱殑鍙兘鎬т腑鐞嗚В鎴戜滑鑷韩銆 ','9787559848048','鍙兘鎬х殑鑹烘湳锛氭瘮杈冩斂娌诲30璁 ','涓枃鍥句功闃呰瀹 1锛堜節榫欐箹A208锛 ','骞胯タ甯堣寖澶у鍑虹増绀 '),(_binary '}锟斤拷锟 \�','鍖楀矝','available','I217.02/1471','https://i.dawnlab.me/e23c468d53e7c4fa5846bd2fac402ae1.png','銆婂繀鏈変汉閲嶅啓鐖辨儏銆嬫槸钁楀悕璇椾汉鍖楀矝鐨勪綔鍝佺簿閫夐泦锛 46绡囩粡鍏歌瘲姝屼笌鏁ｆ枃浣滃搧锛屽憟鐜拌瘲浜恒 佹暎鏂囧銆佺敾鑰呫 佹憚褰辫 呭寳宀涚殑鍒涗綔鍏ㄨ矊銆傗 滀竴璇椾竴鏂団 濈殑缂栨帓鐗硅壊锛屽鍚屼腑鍥借瘲璇濅紶缁熶箣浼犵画銆備粠鈥滄毚椋庨洦鐨勮蹇嗏 濆埌鈥滆瘝鐨勬祦浜♀ 濆埌鈥滃ぇ鍦颁箣涔︹ 濓紝涓夎緫璇楁枃浣滃搧瀹屾暣灞曠幇璇椾汉鑷骞磋嚦浠婄殑浜虹敓琛屾梾锛屾槧鐓т簩鍗佷笘绾笅鍗婂彾婵 鑽＄殑鏃朵唬椋庢櫙銆  鈥滄垜鍙楅泧浜庝竴涓紵澶х殑璁板繂銆傗 濆寳宀涚殑璇楁瓕鏄竴浠ｄ汉鐨勯泦浣撹蹇嗕笌绮剧闀滃儚銆傝嚜鎴戜笌鏃朵唬锛屼粬涔′笌鏁呬埂锛屽巻鍙蹭笌鐜板疄锛屽湪绉嶇鎮栬涓庢柇瑁備腑锛屽湪鍑哄彂涓庢姷杈句箣闂达紝鍖楀矝鎶婅瘝璇瀿杩涘巻鍙层 傚寳宀涚殑鏁ｆ枃鍒欑畝鍑 鏈夊瓒ｏ紝浠庤壘浼︹ ㈤噾鏂牎鐨勭航绾﹀埌鍗″か鍗＄殑甯冩媺鏍硷紝浠庨樋鎷夋硶鐗圭殑鎷夐┈鎷夊埌娲涘皵杩︾殑瑗跨彮鐗欙紝浠栧湪婕傛硦鐨勪笘鐣岃鏃呬腑涓庤韩浠藉悇寮傜殑璇椾汉瀛﹁ 呯瓑鐩搁 ㈢浉闄咃紝鎵 瑙佹墍闂婚矞娲讳害鎯婂績锛涗篃蹇嗗皯骞村線浜嬨 佷翰鍙嬫寶浜わ紝娴撶儓涔℃剚涓庡皷閿愮柤鐥涘父涓嶆湡鑰岃嚦銆傝瘲浜轰箣绗斿啓浣滄暎鏂囷紝鎭㈠姹夎鐨勪赴瀵屻 佹晱閿愩 佹柊椴溿 備功涓害鏈夊骞呭寳宀涚殑缁 ... (灞曞紑鍏ㄩ儴) 鍐呭绠 浠嬶細 銆婂繀鏈変汉閲嶅啓鐖辨儏銆嬫槸钁楀悕璇椾汉鍖楀矝鐨勪綔鍝佺簿閫夐泦锛 46绡囩粡鍏歌瘲姝屼笌鏁ｆ枃浣滃搧锛屽憟鐜拌瘲浜恒 佹暎鏂囧銆佺敾鑰呫 佹憚褰辫 呭寳宀涚殑鍒涗綔鍏ㄨ矊銆傗 滀竴璇椾竴鏂団 濈殑缂栨帓鐗硅壊锛屽鍚屼腑鍥借瘲璇濅紶缁熶箣浼犵画銆備粠鈥滄毚椋庨洦鐨勮蹇嗏 濆埌鈥滆瘝鐨勬祦浜♀ 濆埌鈥滃ぇ鍦颁箣涔︹ 濓紝涓夎緫璇楁枃浣滃搧瀹屾暣灞曠幇璇椾汉鑷骞磋嚦浠婄殑浜虹敓琛屾梾锛屾槧鐓т簩鍗佷笘绾笅鍗婂彾婵 鑽＄殑鏃朵唬椋庢櫙銆  鈥滄垜鍙楅泧浜庝竴涓紵澶х殑璁板繂銆傗 濆寳宀涚殑璇楁瓕鏄竴浠ｄ汉鐨勯泦浣撹蹇嗕笌绮剧闀滃儚銆傝嚜鎴戜笌鏃朵唬锛屼粬涔′笌鏁呬埂锛屽巻鍙蹭笌鐜板疄锛屽湪绉嶇鎮栬涓庢柇瑁備腑锛屽湪鍑哄彂涓庢姷杈句箣闂达紝鍖楀矝鎶婅瘝璇瀿杩涘巻鍙层 傚寳宀涚殑鏁ｆ枃鍒欑畝鍑 鏈夊瓒ｏ紝浠庤壘浼︹ ㈤噾鏂牎鐨勭航绾﹀埌鍗″か鍗＄殑甯冩媺鏍硷紝浠庨樋鎷夋硶鐗圭殑鎷夐┈鎷夊埌娲涘皵杩︾殑瑗跨彮鐗欙紝浠栧湪婕傛硦鐨勪笘鐣岃鏃呬腑涓庤韩浠藉悇寮傜殑璇椾汉瀛﹁ 呯瓑鐩搁 ㈢浉闄咃紝鎵 瑙佹墍闂婚矞娲讳害鎯婂績锛涗篃蹇嗗皯骞村線浜嬨 佷翰鍙嬫寶浜わ紝娴撶儓涔℃剚涓庡皷閿愮柤鐥涘父涓嶆湡鑰岃嚦銆傝瘲浜轰箣绗斿啓浣滄暎鏂囷紝鎭㈠姹夎鐨勪赴瀵屻 佹晱閿愩 佹柊椴溿 備功涓害鏈夊骞呭寳宀涚殑缁樼敾涓庢憚褰变綔鍝侊紝杩欐槸浠栧湪鏂囧瓧涔嬪鎵惧鍒扮殑鍙︿竴绉嶈瑷 銆 ','9787544398565','蹇呮湁浜洪噸鍐欑埍鎯 ','涓枃鍥句功闃呰瀹 1锛堜節榫欐箹A208锛 ','娴峰崡鍑虹増绀 '),(_binary '锟斤拷	锟斤','浼痉,鑸嶆俯','available','K837.126.1/30','https://i.dawnlab.me/8f17fc565e3871bf8ca9ad22da33c3e5.png','銆婂ゥ鏈捣榛樹紶銆嬫槸缇庡浗鈥滃師瀛愬脊涔嬬埗鈥濈綏浼壒路J.濂ユ湰娴烽粯鐨勭涓 閮ㄥ畬鏁翠紶璁帮紝鏇捐崳鑾 2006骞寸編鍥芥櫘鍒╃瓥濂栵紙浼犺绫伙級銆備綔涓轰竴鍚嶆澃鍑虹殑涓斿瘜浜庨瓍鍔涚殑鐗╃悊瀛﹀锛屽ゥ鏈捣榛樺湪绗簩娆′笘鐣屽ぇ鎴樻湡闂翠负缇庡浗璐＄尞浜嗕竴鍒囥 傚師瀛愬脊鍦ㄦ棩鏈箍宀涚垎鐐镐箣鍚庯紝濂ユ湰娴烽粯鎴愪负鍚屼唬浜轰腑鏈 钁楀悕鐨勭瀛﹀锛屽悓鏃朵篃鏄 20涓栫邯鏈 鍏蜂簤璁 х殑浜虹墿涔嬩竴锛屽苟鎴愪负闈㈠绉戝杩涙鐨勭幇浠ｄ汉鐨勫舰璞″寲韬   濂ユ湰娴烽粯鏄鏍哥墿璐ㄥ疄鏂藉浗闄呮帶鍒剁殑婵 杩涙彁妗堢殑鍙戣捣鑰呬箣涓 锛岃繖涓 鎬濇兂鍗充娇鍦ㄤ粖澶╀篃鏄嚦鍏抽噸瑕佺殑銆備粬鏋佸姏鍙嶅缇庡浗鍙戝睍姘㈠脊锛屽苟寮虹儓鍙嶅缇庡浗绌哄啗璇曞浘鍙戝姩涓 鍦烘瀬鍏跺嵄闄╃殑鏍告垬浜夌殑璁″垝銆 20涓栫邯50骞翠唬鍒濇槸涓 涓厖婊＄檾鐥囩殑骞翠唬锛屽ゥ鏈捣榛樼殑鎬濇兂鑷劧灏辨垚涓哄己鍔涙敮鎸佸缓閫犲ぇ瑙勬ā鏉 浼ゆ ф鍣ㄧ殑浜轰滑璇呭拻鐨勫璞° 備綔涓哄洖搴旓紝缇庡浗鍘熷瓙鑳藉鍛樹細涓诲腑鍒樻槗鏂锋柉鐗瑰姵鏂 佽秴绾ф牳寮圭殑鏀寔鑰呯埍寰峰崕路鐗瑰嫆锛屼互鍙婄編鍥借仈閭﹁皟鏌ュ眬灞 闀垮焹寰峰姞路鑳′經鍦ㄥ箷鍚庣簿蹇冪瓥鍒掍簡涓 鍦哄畨鍏ㄥ惉璇佷細锛屼粠鑰屽垏鏂簡濂ユ湰娴烽粯涓庣編鍥芥牳绉樺瘑鐨勮仈绯汇   銆婂ゥ鏈捣榛樹紶銆嬩互鍓嶆墍鏈湁鐨勭粏鑺傛彮绀轰簡濂ユ湰娴烽粯鐨勭敓娲诲強鍏舵墍澶勭殑鏃朵唬銆備綔鑰呴 氳繃浠庣編鍥藉浗鍐呭拰鍥藉鐨勮祫鏂欓鎼滈泦鏉ョ殑澶ч噺璁板綍鍜屼俊浠躲 佺編鍥借仈閭﹁皟鏌ュ眬鐨勬。妗堬紝浠ュ強涓庡ゥ鏈捣榛樻渶浜茶繎鐨勬湅鍙嬨 佷翰鎴氬拰鍚屼簨鐨勮璋堬紝瀵瑰ゥ鏈捣榛樼殑涓 鐢熶綔浜嗚灏芥棤閬楃殑鐮旂┒銆 20涓栫邯鍒濓紝濂ユ湰娴烽粯鍦ㄧ航绾﹀競鐨勯亾寰锋枃鍖栧鏍℃帴鍙椾簡鏃╂湡鏁欒偛锛屼箣鍚庡湪鍝堜經澶у鍜屽墤妗ュぇ瀛﹂亣鍒颁簡浠栦汉鐢熺殑绗竴娆′釜浜哄嵄鏈恒 備簬鏄粬鍘讳簡寰峰浗锛屽湪閭ｉ噷锛屼粬璺熶笘鐣屼笂鏈 鏈夋垚灏辩殑鐞嗚瀹朵滑瀛︿範浜嗛噺瀛愮墿鐞嗗銆傝 屽悗锛屼粬鍦ㄧ編鍥藉姞鍒╃灏间簹鐨勪集鍏嬪埄寤虹珛浜嗗浗鍐呬竴娴佺殑鐞嗚鐗╃悊瀛﹂櫌銆傚湪閭ｉ噷锛屼粬杩樻繁娣卞湴鍗峰叆浜嗙ぞ浼氬叕姝ｄ簨涓氾紝骞朵笌寰堝鍏变骇鍏氫汉淇濇寔鐫 鑱旂郴銆傚悗鏉ワ紝浠栧幓浜嗘柊澧ㄨタ鍝ュ窞鐨勬礇鏂樋鎷夎帿鏂紝骞跺湪閭ｉ噷寤虹珛浜嗕笘鐣屼笂鏈 鍏锋綔鍔涚殑鏍告鍣ㄥ疄楠屽锛涗笌姝ゅ悓鏃讹紝浠栦篃鏀瑰彉浜嗚嚜宸便  1947骞磋嚦1966骞达紝浠栦竴鐩存媴浠荤編鍥芥櫘鏋楁柉椤块珮绛夌爺绌堕櫌鐨勪富绠′竴鑱屻   銆婂ゥ鏈捣榛樹紶銆嬬敓鍔ㄥ嬀鐢诲嚭浜嗕竴浣嶆澃鍑虹殑銆佸厖婊℃姳璐熺殑銆佸鏉傜殑銆佹湁缂虹偣鐨勪汉鐗╁舰璞★紝浠栦笌缇庡浗鐨勨 滃ぇ钀ф潯鈥濄 佺浜屾涓栫晫澶ф垬鍜屽喎鎴樻湁鐫 娣卞埢鐨勫叧鑱斻 傚畠涓嶄絾鏄竴閮ㄥ厖婊″巻鍙叉劅鐨勪汉鐗╀紶璁帮紝鑰屼笖瀵逛簬鎴戜滑鐞嗚В褰撲笅浠ュ強鏈潵鐨勯 夋嫨閮戒細鍏锋湁閲嶈鐨勫惎绀恒  ','9787544710008','濂ユ湰娴烽粯浼 ','涔濋緳婀栨潕鏂囨鍥句功棣 ','璇戞灄鍑虹増绀 '),(_binary '锟絶锟斤拷{','浼痉,鑸嶆俯','available','K837.126.1/30','https://i.dawnlab.me/8f17fc565e3871bf8ca9ad22da33c3e5.png','銆婂ゥ鏈捣榛樹紶銆嬫槸缇庡浗鈥滃師瀛愬脊涔嬬埗鈥濈綏浼壒路J.濂ユ湰娴烽粯鐨勭涓 閮ㄥ畬鏁翠紶璁帮紝鏇捐崳鑾 2006骞寸編鍥芥櫘鍒╃瓥濂栵紙浼犺绫伙級銆備綔涓轰竴鍚嶆澃鍑虹殑涓斿瘜浜庨瓍鍔涚殑鐗╃悊瀛﹀锛屽ゥ鏈捣榛樺湪绗簩娆′笘鐣屽ぇ鎴樻湡闂翠负缇庡浗璐＄尞浜嗕竴鍒囥 傚師瀛愬脊鍦ㄦ棩鏈箍宀涚垎鐐镐箣鍚庯紝濂ユ湰娴烽粯鎴愪负鍚屼唬浜轰腑鏈 钁楀悕鐨勭瀛﹀锛屽悓鏃朵篃鏄 20涓栫邯鏈 鍏蜂簤璁 х殑浜虹墿涔嬩竴锛屽苟鎴愪负闈㈠绉戝杩涙鐨勭幇浠ｄ汉鐨勫舰璞″寲韬   濂ユ湰娴烽粯鏄鏍哥墿璐ㄥ疄鏂藉浗闄呮帶鍒剁殑婵 杩涙彁妗堢殑鍙戣捣鑰呬箣涓 锛岃繖涓 鎬濇兂鍗充娇鍦ㄤ粖澶╀篃鏄嚦鍏抽噸瑕佺殑銆備粬鏋佸姏鍙嶅缇庡浗鍙戝睍姘㈠脊锛屽苟寮虹儓鍙嶅缇庡浗绌哄啗璇曞浘鍙戝姩涓 鍦烘瀬鍏跺嵄闄╃殑鏍告垬浜夌殑璁″垝銆 20涓栫邯50骞翠唬鍒濇槸涓 涓厖婊＄檾鐥囩殑骞翠唬锛屽ゥ鏈捣榛樼殑鎬濇兂鑷劧灏辨垚涓哄己鍔涙敮鎸佸缓閫犲ぇ瑙勬ā鏉 浼ゆ ф鍣ㄧ殑浜轰滑璇呭拻鐨勫璞° 備綔涓哄洖搴旓紝缇庡浗鍘熷瓙鑳藉鍛樹細涓诲腑鍒樻槗鏂锋柉鐗瑰姵鏂 佽秴绾ф牳寮圭殑鏀寔鑰呯埍寰峰崕路鐗瑰嫆锛屼互鍙婄編鍥借仈閭﹁皟鏌ュ眬灞 闀垮焹寰峰姞路鑳′經鍦ㄥ箷鍚庣簿蹇冪瓥鍒掍簡涓 鍦哄畨鍏ㄥ惉璇佷細锛屼粠鑰屽垏鏂簡濂ユ湰娴烽粯涓庣編鍥芥牳绉樺瘑鐨勮仈绯汇   銆婂ゥ鏈捣榛樹紶銆嬩互鍓嶆墍鏈湁鐨勭粏鑺傛彮绀轰簡濂ユ湰娴烽粯鐨勭敓娲诲強鍏舵墍澶勭殑鏃朵唬銆備綔鑰呴 氳繃浠庣編鍥藉浗鍐呭拰鍥藉鐨勮祫鏂欓鎼滈泦鏉ョ殑澶ч噺璁板綍鍜屼俊浠躲 佺編鍥借仈閭﹁皟鏌ュ眬鐨勬。妗堬紝浠ュ強涓庡ゥ鏈捣榛樻渶浜茶繎鐨勬湅鍙嬨 佷翰鎴氬拰鍚屼簨鐨勮璋堬紝瀵瑰ゥ鏈捣榛樼殑涓 鐢熶綔浜嗚灏芥棤閬楃殑鐮旂┒銆 20涓栫邯鍒濓紝濂ユ湰娴烽粯鍦ㄧ航绾﹀競鐨勯亾寰锋枃鍖栧鏍℃帴鍙椾簡鏃╂湡鏁欒偛锛屼箣鍚庡湪鍝堜經澶у鍜屽墤妗ュぇ瀛﹂亣鍒颁簡浠栦汉鐢熺殑绗竴娆′釜浜哄嵄鏈恒 備簬鏄粬鍘讳簡寰峰浗锛屽湪閭ｉ噷锛屼粬璺熶笘鐣屼笂鏈 鏈夋垚灏辩殑鐞嗚瀹朵滑瀛︿範浜嗛噺瀛愮墿鐞嗗銆傝 屽悗锛屼粬鍦ㄧ編鍥藉姞鍒╃灏间簹鐨勪集鍏嬪埄寤虹珛浜嗗浗鍐呬竴娴佺殑鐞嗚鐗╃悊瀛﹂櫌銆傚湪閭ｉ噷锛屼粬杩樻繁娣卞湴鍗峰叆浜嗙ぞ浼氬叕姝ｄ簨涓氾紝骞朵笌寰堝鍏变骇鍏氫汉淇濇寔鐫 鑱旂郴銆傚悗鏉ワ紝浠栧幓浜嗘柊澧ㄨタ鍝ュ窞鐨勬礇鏂樋鎷夎帿鏂紝骞跺湪閭ｉ噷寤虹珛浜嗕笘鐣屼笂鏈 鍏锋綔鍔涚殑鏍告鍣ㄥ疄楠屽锛涗笌姝ゅ悓鏃讹紝浠栦篃鏀瑰彉浜嗚嚜宸便  1947骞磋嚦1966骞达紝浠栦竴鐩存媴浠荤編鍥芥櫘鏋楁柉椤块珮绛夌爺绌堕櫌鐨勪富绠′竴鑱屻   銆婂ゥ鏈捣榛樹紶銆嬬敓鍔ㄥ嬀鐢诲嚭浜嗕竴浣嶆澃鍑虹殑銆佸厖婊℃姳璐熺殑銆佸鏉傜殑銆佹湁缂虹偣鐨勪汉鐗╁舰璞★紝浠栦笌缇庡浗鐨勨 滃ぇ钀ф潯鈥濄 佺浜屾涓栫晫澶ф垬鍜屽喎鎴樻湁鐫 娣卞埢鐨勫叧鑱斻 傚畠涓嶄絾鏄竴閮ㄥ厖婊″巻鍙叉劅鐨勪汉鐗╀紶璁帮紝鑰屼笖瀵逛簬鎴戜滑鐞嗚В褰撲笅浠ュ強鏈潵鐨勯 夋嫨閮戒細鍏锋湁閲嶈鐨勫惎绀恒  ','9787544710008','濂ユ湰娴烽粯浼 ','涔濋緳婀栨潕鏂囨鍥句功棣 ','璇戞灄鍑虹増绀 ');
/*!40000 ALTER TABLE `book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `card_transaction`
--

DROP TABLE IF EXISTS `card_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `card_transaction` (
  `uuid` binary(16) NOT NULL,
  `amount` int DEFAULT NULL,
  `cardNumber` int NOT NULL,
  `time` datetime(6) DEFAULT NULL,
  `type` enum('deposit','payment') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `card_transaction`
--

LOCK TABLES `card_transaction` WRITE;
/*!40000 ALTER TABLE `card_transaction` DISABLE KEYS */;
INSERT INTO `card_transaction` VALUES (_binary 's 裄FP猏蕒#',24900,123456,'2025-09-10 09:22:00.699000','payment','鍟嗗簵娑堣垂'),(_binary '龆L蘉C]\�',1170300,123456,'2025-09-10 09:24:00.695000','payment','鍟嗗簵娑堣垂'),(_binary '\nv锟较锟\�',100000,213210000,'2023-09-11 22:46:35.888000','deposit','鍏呭  '),(_binary '锟 \0\n锟絀\�',2028950,123456,'2023-09-15 03:24:38.503000','payment','鍟嗗簵娑堣垂'),(_binary '锟 /}锟斤拷',1269900,123456,'2023-09-15 08:16:16.785000','payment','鍟嗗簵娑堣垂'),(_binary ' 榎趼稤鶟 $',1234567800,123456,'2025-09-10 10:40:59.878000','deposit','鍏呭  '),(_binary '*锟斤拷锟斤',1294800,123456,'2023-09-15 08:28:38.118000','payment','鍟嗗簵娑堣垂'),(_binary '0褌锟斤拷uGT',0,123456,'2023-09-14 23:28:54.640000','payment','鍟嗗簵娑堣垂'),(_binary '1锟斤拷锟\�',157700,123456,'2023-09-12 13:00:53.842000','payment','鍟嗗簵娑堣垂'),(_binary '<|锟 锟紺锟',24900,123456,'2023-09-15 08:16:05.103000','payment','鍟嗗簵娑堣垂'),(_binary 'Mg锟斤拷锟 .',13950,213210000,'2023-09-15 08:59:01.587000','payment','鍟嗗簵娑堣垂'),(_binary 'b锟紾锟斤拷',1359800,123456,'2023-09-14 23:32:24.237000','payment','鍟嗗簵娑堣垂'),(_binary 'e+ 锟絆FM锟 ',2639700,123456,'2023-09-15 00:22:12.784000','payment','鍟嗗簵娑堣垂'),(_binary 'h锟 &n`E酆Ir\r',72900,123456,'2023-09-14 16:17:49.292000','payment','鍟嗗簵娑堣垂'),(_binary '朞\"㘎虭n扆\�',0,123456,'2025-09-10 09:26:40.455000','payment','鍟嗗簵娑堣垂'),(_binary '諓锟 3锟斤\�',199800,123456,'2023-09-15 08:16:08.471000','payment','鍟嗗簵娑堣垂'),(_binary '锟 锟絴3PK~\�',100000000,123456,'2023-09-14 23:35:24.627000','deposit','鍏呭  '),(_binary '锟 +锟紸锟\�',1000000,123456,'2023-09-14 23:32:18.517000','deposit','鍏呭  '),(_binary '锟斤拷锟\�',165400,123456,'2023-09-13 09:11:02.755000','payment','鍟嗗簵娑堣垂'),(_binary '锟斤拷(r锟\�',165400,123456,'2023-09-11 22:37:34.293000','payment','鍟嗗簵娑堣垂'),(_binary '锟斤拷5锟\�',1000000,123456,'2023-09-11 22:37:25.676000','deposit','鍏呭  '),(_binary '锟斤拷锟斤\�',1269900,123456,'2023-09-15 08:16:44.201000','payment','鍟嗗簵娑堣垂'),(_binary '锟絍锟 1锟\�',1269900,123456,'2023-09-15 01:12:45.541000','payment','鍟嗗簵娑堣垂'),(_binary '锟絔eS~_Hv锟\�',1269900,123456,'2023-09-15 08:16:29.661000','payment','鍟嗗簵娑堣垂');
/*!40000 ALTER TABLE `card_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class`
--

DROP TABLE IF EXISTS `class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class` (
  `uuid` binary(16) NOT NULL,
  `capacity` int NOT NULL,
  `courseUuid` binary(16) NOT NULL,
  `place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `schedule` json NOT NULL,
  `teacherId` int NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class`
--

LOCK TABLES `class` WRITE;
/*!40000 ALTER TABLE `class` DISABLE KEYS */;
INSERT INTO `class` VALUES (_binary 'B锟絋锟斤拷',60,_binary '锟 W|锟紷\�','鏁欎笁-104','[{\"first\": {\"first\": 9, \"second\": 16}, \"second\": {\"first\": 2, \"second\": {\"first\": 11, \"second\": 12}}}, {\"first\": {\"first\": 9, \"second\": 16}, \"second\": {\"first\": 4, \"second\": {\"first\": 3, \"second\": 4}}}]',109000600),(_binary 'Mw锟 U锟組f\�',60,_binary '锟 .锟斤拷K|','閲戞櫤妤 ','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 2, \"second\": {\"first\": 1, \"second\": 4}}}, {\"first\": {\"first\": 4, \"second\": 8}, \"second\": {\"first\": 5, \"second\": {\"first\": 6, \"second\": 8}}}]',109000600),(_binary 'i锟 ><锟 )@跂',60,_binary '锟斤拷锟絴d','鏁欏洓-301','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 4, \"second\": {\"first\": 3, \"second\": 5}}}]',109000600),(_binary 'kF锟絓n锟紾',15,_binary '锟斤拷锟斤\�','鏁欏叓 201','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 1, \"second\": {\"first\": 1, \"second\": 4}}}]',109000601),(_binary 'r锟斤拷锟斤',60,_binary '锟絁P锟 >JBE\�','鏁欎笁-104','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 2, \"second\": {\"first\": 6, \"second\": 7}}}]',109000604),(_binary '锟斤拷D佼:II',20,_binary '锟斤拷锟斤\�','鏁欏叓 303','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 3, \"second\": {\"first\": 1, \"second\": 4}}}]',109000602),(_binary '锟絓r-W锟斤\�',30,_binary '锟 8jd锟絓rD\"','涓滀節妤 ','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 1, \"second\": {\"first\": 1, \"second\": 2}}}]',123456);
/*!40000 ALTER TABLE `class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `uuid` binary(16) NOT NULL,
  `courseId` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `courseName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `credit` float NOT NULL,
  `school` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (_binary '锟 W|锟紷\�','B09H1040','鎿嶄綔绯荤粺涓撻瀹炶返',4,'璁＄畻鏈虹瀛︿笌宸ョ▼瀛﹂櫌'),(_binary '锟 .锟斤拷K|','BJSL0456','涓撲笟鎶 鑳藉疄璁 ',3,'璁＄畻鏈虹瀛︿笌宸ョ▼瀛﹂櫌'),(_binary '锟 8jd锟絓rD\"','BJSL0081','杞欢宸ョ▼',4,'璁＄畻鏈虹瀛︿笌宸ョ▼瀛﹂櫌'),(_binary '锟斤拷锟斤\�','BJSL0123','Kotlin 101',4,'璁＄畻鏈虹瀛︿笌宸ョ▼瀛﹂櫌'),(_binary '锟斤拷锟絴d','B09T1070','璁＄畻鏈轰笌绀句細',2,'璁＄畻鏈虹瀛︿笌宸ョ▼瀛﹂櫌'),(_binary '锟絁P锟 >JBE\�','B71S0030','缂栬瘧鍘熺悊',4,'璁＄畻鏈虹瀛︿笌宸ョ▼瀛﹂櫌');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `finance_card`
--

DROP TABLE IF EXISTS `finance_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finance_card` (
  `cardNumber` int NOT NULL,
  `balance` int DEFAULT NULL,
  `status` enum('frozen','lost','normal') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`cardNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `finance_card`
--

LOCK TABLES `finance_card` WRITE;
/*!40000 ALTER TABLE `finance_card` DISABLE KEYS */;
INSERT INTO `finance_card` VALUES (123456,1322183650,'normal'),(213210000,86050,'normal'),(213210008,0,'normal');
/*!40000 ALTER TABLE `finance_card` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gpt`
--

DROP TABLE IF EXISTS `gpt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gpt` (
  `cardNumber` int NOT NULL,
  `context` longtext,
  PRIMARY KEY (`cardNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gpt`
--

LOCK TABLES `gpt` WRITE;
/*!40000 ALTER TABLE `gpt` DISABLE KEYS */;
INSERT INTO `gpt` VALUES (123456,'{\"1b55eaff-6589-4e01-8653-21f7d0305fa0\":{\"sessionId\":\"1b55eaff-6589-4e01-8653-21f7d0305fa0\",\"title\":\"测试\",\"lastModified\":1757776289125,\"messageHistory\":[{\"id\":\"916ba23f-1b70-4f86-8d53-3cc51d7fae98\",\"message\":{\"map\":{\"role\":\"system\",\"content\":\"你是一个乐于助人的AI助手Assistant-DeepSeek, 正在和一个学生对话，今天是9月10日。\"}}},{\"id\":\"9c0d3b5f-73e1-4926-9ac8-3b47fc650918\",\"message\":{\"map\":{\"role\":\"user\",\"content\":\"测试\"}}},{\"id\":\"9fe826b5-612a-4921-914b-5e06e7115507\",\"message\":{\"map\":{\"role\":\"assistant\",\"content\":\"你好！我是DeepSeek-V3，很高兴为你提供帮助。请问你想测试什么内容？如果有具体的问题或需求，可以告诉我，我会尽力协助你！😊\"}}}]}}');
/*!40000 ALTER TABLE `gpt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `library_transaction`
--

DROP TABLE IF EXISTS `library_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `library_transaction` (
  `uuid` binary(16) NOT NULL,
  `bookUuid` binary(16) NOT NULL,
  `borrowTime` datetime(6) NOT NULL,
  `returnTime` datetime(6) DEFAULT NULL,
  `userId` int NOT NULL,
  `dueTime` datetime(6) NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `library_transaction`
--

LOCK TABLES `library_transaction` WRITE;
/*!40000 ALTER TABLE `library_transaction` DISABLE KEYS */;
INSERT INTO `library_transaction` VALUES (_binary 'l锟斤拷锟 ',_binary '锟斤拷	锟斤','2023-09-15 08:57:50.819000','2023-09-15 08:57:58.937000',213210000,'2023-10-15 08:57:50.819000'),(_binary 'lR锟斤拷H\�',_binary '7f锟絨\0%N锟\�','2023-09-15 08:55:58.701000',NULL,213210000,'2023-10-15 08:55:58.701000');
/*!40000 ALTER TABLE `library_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `select_record`
--

DROP TABLE IF EXISTS `select_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `select_record` (
  `uuid` binary(16) NOT NULL,
  `cardNumber` int NOT NULL,
  `classUuid` binary(16) DEFAULT NULL,
  `grade` json DEFAULT NULL,
  `selectTime` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `select_record`
--

LOCK TABLES `select_record` WRITE;
/*!40000 ALTER TABLE `select_record` DISABLE KEYS */;
INSERT INTO `select_record` VALUES (_binary '7@J锟 =L2锟 ',213210000,_binary 'Mw锟 U锟組f\�',NULL,'2023-09-15 08:27:06.373000'),(_binary 'Ti\n锟 O&锟\�',213210004,_binary '锟斤拷D佼:II',NULL,'2023-09-15 08:39:20.309000'),(_binary '锟 g锟 F锟',213210012,_binary 'Mw锟 U锟組f\�',NULL,'2023-09-15 08:40:08.592000'),(_binary '$锟絟h锟絆!',213210003,_binary 'i锟 ><锟 )@跂',NULL,'2023-09-15 08:38:36.651000'),(_binary '%锟絓n锟斤\�',213210012,_binary 'B锟絋锟斤拷',NULL,'2023-09-15 08:40:14.161000'),(_binary 'M锟斤拷%]M\�',213210003,_binary 'Mw锟 U锟組f\�',NULL,'2023-09-15 08:38:31.159000'),(_binary 'NBgE锟絁虖E\n',213210008,_binary 'r锟斤拷锟斤',NULL,'2023-09-15 08:39:48.488000'),(_binary 'P锟斤拷]锟\�',213210003,_binary '锟斤拷D佼:II',NULL,'2023-09-15 08:38:42.265000'),(_binary 'S<锟絇锟 I\�',213210003,_binary '锟絓r-W锟斤\�',NULL,'2023-09-15 08:38:35.119000'),(_binary 'U3\Z锟紾锟\�',123456,_binary '锟斤拷D佼:II',NULL,'2023-09-15 01:17:45.437000'),(_binary ']锟斤拷锟 ',213210004,_binary 'r锟斤拷锟斤',NULL,'2023-09-15 08:39:16.860000'),(_binary 'e锟絋锟斤拷',213210004,_binary '锟絓r-W锟斤\�',NULL,'2023-09-15 08:39:15.574000'),(_binary 'fv锟 ,OG*锟\�',213210004,_binary 'B锟絋锟斤拷',NULL,'2023-09-15 08:39:12.835000'),(_binary 'hy锟 	锟斤拷',213210000,_binary '锟絓r-W锟斤\�',NULL,'2023-09-15 08:27:03.828000'),(_binary 'x锟斤拷锟 ',213210008,_binary 'kF锟絓n锟紾',NULL,'2023-09-15 08:39:45.721000'),(_binary '菁e锟 -锟紷U',213210004,_binary 'Mw锟 U锟組f\�',NULL,'2023-09-15 08:39:10.098000'),(_binary '锟 fB\\锟  ',213210012,_binary '锟斤拷D佼:II',NULL,'2023-09-15 08:40:28.664000'),(_binary '锟 )r锟 L锟',213210003,_binary 'r锟斤拷锟斤',NULL,'2023-09-15 08:38:38.486000'),(_binary '锟 %锟組\Z锟\�',213210000,_binary 'B锟絋锟斤拷',NULL,'2023-09-15 08:26:59.506000'),(_binary '锟 )l锟斤拷A',213210008,_binary 'Mw锟 U锟組f\�',NULL,'2023-09-15 08:39:39.374000'),(_binary '锟侥 ?N锟\�',213210008,_binary 'i锟 ><锟 )@跂',NULL,'2023-09-15 08:39:41.329000'),(_binary '锟斤拷锟斤\�',213210000,_binary 'r锟斤拷锟斤',NULL,'2023-09-15 08:27:11.810000'),(_binary '锟絏锟斤拷\�',213210012,_binary 'r锟斤拷锟斤',NULL,'2023-09-15 08:40:17.860000'),(_binary '锟絖7锟桔僊',123456,_binary 'B锟絋锟斤拷',NULL,'2023-09-15 03:22:34.196000'),(_binary '锟絥~锟斤拷',213210012,_binary '锟絓r-W锟斤\�',NULL,'2023-09-15 08:40:25.677000'),(_binary '锟絫锟 锟\�',123456,_binary '锟絓r-W锟斤\�',NULL,'2023-09-15 01:17:41.798000');
/*!40000 ALTER TABLE `select_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_item`
--

DROP TABLE IF EXISTS `store_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_item` (
  `uuid` binary(16) NOT NULL,
  `barcode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `itemName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `pictureLink` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` int NOT NULL,
  `salesVolume` int NOT NULL,
  `stock` int NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_item`
--

LOCK TABLES `store_item` WRITE;
/*!40000 ALTER TABLE `store_item` DISABLE KEYS */;
INSERT INTO `store_item` VALUES (_binary '\Z锟 .锟 L锟','01234567891335','璧炵編澶槼锛 ','姝ｇ増 榛戞殫涔嬮瓊瀹樻柟鑹烘湳璁惧畾闆嗗叏濂 1-2-3鍐  DARK SOULS','https://i.dawnlab.me/864b843fd26967485f02c0628911531f.png',24900,50,0),(_binary '=锟 ;锟斤拷\�','154442959971','涓撲笟褰曢煶楹﹀厠椋 ','Sony/绱㈠凹 ECM-G1 鏋瀷楹﹀厠椋  澶у昂瀵告敹闊冲崟鍏  娓呮櫚浜哄０鏀跺綍\n','https://i.dawnlab.me/633e9d5e7835e1f00aa7d5506d119ac6.png',99900,3,41),(_binary '@f锟紿锟斤\�','4571367890123','MBA','Apple/鑻规灉 13 鑻卞 MacBook Air Apple M2 鑺墖 8 鏍镐腑澶鐞嗗櫒 8 鏍稿浘褰㈠鐞嗗櫒 8GB 缁熶竴鍐呭瓨 256GB 鍥烘 佺‖鐩 ','https://i.dawnlab.me/b214883312422e9a7149422c19d6ebd5.png',1269900,6,47),(_binary 'Rw o|	@#锟 锟','32145540123','c++鍦ｇ粡锛侊紒','C++Primer涓枃鐗  绗簲鐗  璁＄畻鏈哄紑鍙慶璇█浠庡叆闂ㄥ埌绮鹃  ','https://i.dawnlab.me/b5b1080d399f9faf04c8c78a02ec0eab.png',6250,2,93),(_binary 'z\'锟斤拷锟\�','100042959971','鍚归鏈烘祴璇 ','绫冲 灏忕背鐢靛惞椋  鍚归鏈  璐熺瀛愭姢鍙戝彲鎶樺彔 澶ч鍔涢 熷共 H101 鐧借壊','https://i.dawnlab.me/269e038d982f78bb02c3e4ba3a06f8c9.png',7700,2,29),(_binary '锟絳锟紼锟\�','3214567890123','鎰熻涓嶅鍘熺','銆愬師绁炲畼鏂 /鍏ㄦ銆戝埢鏅绰烽渾闇撳揩闆╒er.1/7鎵嬪姙Genshin','https://i.dawnlab.me/520ff42fc04fa57487b6fb65ca277857.png',84800,1,96),(_binary '锟缴癸拷RwG5','9874567890123','涓浗涔嬪厜锛侀仴閬ラ鍏堬紒','HUAWEI Mate 60 Pro 12GB+1TB 闆呭窛闈 ','https://i.dawnlab.me/f7f7ad266921f1eb4fa25c8dceb10842.png',679900,1,17),(_binary '锟脚  锟 ?G\�','0123456789122','鍘熺锛屽惎鍔紒','銆愬師绁炲畼鏂 /鍏ㄦ銆戠敇闆峰惊寰畧鏈圴er.1/7鎵嬪姙 Genshin','https://i.dawnlab.me/654dfbf1dac9573d5fee4b0a8feb0f18.png',72900,60,40);
/*!40000 ALTER TABLE `store_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_transaction`
--

DROP TABLE IF EXISTS `store_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_transaction` (
  `uuid` binary(16) NOT NULL,
  `amount` int NOT NULL,
  `cardNumber` int NOT NULL,
  `itemPrice` int NOT NULL,
  `itemUUID` binary(16) NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `time` datetime(6) NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_transaction`
--

LOCK TABLES `store_transaction` WRITE;
/*!40000 ALTER TABLE `store_transaction` DISABLE KEYS */;
INSERT INTO `store_transaction` VALUES (_binary '%佗锟斤拷H}',1,123456,72900,_binary '锟脚  锟 ?G\�','','2023-09-12 13:00:53.842000'),(_binary '锟絊锟 1ZEB\�',1,123456,72900,_binary '锟脚  锟 ?G\�','','2023-09-14 16:17:49.286000'),(_binary '*#2X锟斤拷AF\�',1,123456,72900,_binary '锟脚  锟 ?G\�','','2023-09-11 22:37:34.292000'),(_binary '.et锟 ?锟紸W\�',1,123456,1269900,_binary '@f锟紿锟斤\�','','2023-09-15 08:16:16.785000'),(_binary '1睉辕虰k眱>z',1,123456,24900,_binary '\Z锟 .锟 L锟','','2025-09-10 09:22:00.689000'),(_binary 'J锟 +\Z&7Ei锟\�',1,123456,84800,_binary '锟絳锟紼锟\�','','2023-09-12 13:00:53.828000'),(_binary '_锟斤拷zgFKe\�',1,123456,1269900,_binary '@f锟紿锟斤\�','','2023-09-15 01:12:45.533000'),(_binary '`Xo锟 ?锟絀c\�',1,123456,7700,_binary 'z\'锟斤拷锟\�','','2023-09-11 22:37:34.278000'),(_binary 'ak锟絋mLe锟\�',1,123456,24900,_binary '\Z锟 .锟 L锟','','2023-09-15 08:16:05.093000'),(_binary 'lnDMM4锟 锟',1,123456,84800,_binary '锟絳锟紼锟\�','','2023-09-13 09:11:02.747000'),(_binary 'o锟斤拷锟 /j',1,213210000,7700,_binary 'z\'锟斤拷锟\�','','2023-09-15 08:59:01.587000'),(_binary 's,锟 3F锟斤',1,123456,84800,_binary '锟絳锟紼锟\�','','2023-09-11 22:37:34.285000'),(_binary 't|of2锟紷锟\�',1,123456,72900,_binary '锟脚  锟 ?G\�','','2023-09-15 03:24:38.503000'),(_binary 'v;锟絞F锟\�',1,123456,72900,_binary '锟脚  锟 ?G\�','','2023-09-13 09:11:02.754000'),(_binary '锟絨锟斤拷',1,123456,1269900,_binary '@f锟紿锟斤\�','','2023-09-15 03:24:38.493000'),(_binary '窚膅煞D皝[78',47,123456,24900,_binary '\Z锟 .锟 L锟','','2025-09-10 09:24:00.694000'),(_binary '锟 锟 3g7H锟',2,123456,1269900,_binary '@f锟紿锟斤\�','','2023-09-15 00:22:12.784000'),(_binary '锟 #\\f锟紾锟',2,123456,679900,_binary '锟缴癸拷RwG5','','2023-09-14 23:32:24.237000'),(_binary '锟斤拷 锟\�',1,123456,1269900,_binary '@f锟紿锟斤\�','','2023-09-15 08:16:44.201000'),(_binary '锟斤拷\Z锟 \�',1,123456,99900,_binary '=锟 ;锟斤拷\�','','2023-09-15 00:22:12.775000'),(_binary '锟斤拷A锟絓',1,123456,1269900,_binary '@f锟紿锟斤\�','','2023-09-15 08:28:38.117000'),(_binary '锟斤拷锟斤\�',1,123456,6250,_binary 'Rw o|	@#锟 锟','','2023-09-15 03:24:38.498000'),(_binary '锟紻锟絲tL*',1,123456,679900,_binary '锟缴癸拷RwG5','','2023-09-15 03:24:38.500000'),(_binary '锟絀軝锟 0F\�',1,123456,7700,_binary 'z\'锟斤拷锟\�','','2023-09-13 09:11:02.739000'),(_binary '锟絟锟斤拷l',2,123456,99900,_binary '=锟 ;锟斤拷\�','','2023-09-15 08:16:08.471000'),(_binary '锟絢锟 7\rC`\�',1,123456,24900,_binary '\Z锟 .锟 L锟','','2023-09-15 08:28:38.099000'),(_binary '锟給t_B锟\�',1,123456,1269900,_binary '@f锟紿锟斤\�','','2023-09-15 08:16:29.661000'),(_binary '锟統褨n锟絀',1,213210000,6250,_binary 'Rw o|	@#锟 锟','','2023-09-15 08:59:01.581000');
/*!40000 ALTER TABLE `store_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `cardNumber` int NOT NULL,
  `birthDate` datetime(6) DEFAULT NULL,
  `birthPlace` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `familyName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` enum('female','male','unspecified') COLLATE utf8mb4_unicode_ci NOT NULL,
  `givenName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `major` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `politicalStatus` enum('CommunistPartyOfChina','CommunistYouthLeagueMember','MDCMember','Masses','ProbationaryPartyMember') COLLATE utf8mb4_unicode_ci NOT NULL,
  `school` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('dropout','expelled','graduated','inSchool','suspended') COLLATE utf8mb4_unicode_ci NOT NULL,
  `studentNumber` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`cardNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (123456,'2003-12-31 00:00:00.000000','姹熻嫃','鐜 ','male','灏忔槑','璁＄畻鏈虹瀛︿笌鎶 鏈 ','Masses','璁＄畻鏈虹瀛︿笌宸ョ▼瀛﹂櫌','graduated','09021200'),(213210000,'2003-12-31 00:00:00.000000','娴欐睙','鏉 ','male','鎯 ','涓村簥鍖诲','CommunistPartyOfChina','鍖诲闄 ','inSchool','11021100'),(213210001,'2003-12-31 00:00:00.000000','娴欐睙','鏉 ','male','鍗 ','涓村簥鍖诲','CommunistPartyOfChina','鍖诲闄 ','inSchool','11021101'),(213210002,'2003-12-31 00:00:00.000000','娴峰崡','寮 ','male','浼 ','寤虹瓚瀛 ','Masses','寤虹瓚瀛﹂櫌','inSchool','11021102'),(213210003,'2003-12-31 00:00:00.000000','绂忓缓','鏉 ','male','鎬濇  ','浜哄伐鏅鸿兘','CommunistPartyOfChina','浜哄伐鏅鸿兘瀛﹂櫌','inSchool','11021103'),(213210004,'2003-12-31 00:00:00.000000','姹熻タ','寮 ','male','鏂囪僵','杞欢宸ョ▼','Masses','杞欢瀛﹂櫌','inSchool','11021104'),(213210005,'2003-12-31 00:00:00.000000','瀹夊窘','鐜 ','female','澶╀竴','缃戠粶绌洪棿瀹夊叏','CommunistPartyOfChina','缃戠粶绌洪棿瀹夊叏瀛﹂櫌','inSchool','11021105'),(213210006,'2003-12-31 00:00:00.000000','鍖椾含','鍒 ','male','娴锋磱','淇℃伅','Masses','鐢靛瓙淇℃伅瀛﹂櫌','inSchool','11021106'),(213210007,'2003-12-31 00:00:00.000000','涓婃捣','闄 ','male','灏忔槬','鍦熸湪宸ョ▼','CommunistPartyOfChina','浜ら 氬闄 ','inSchool','11021107'),(213210008,'2003-12-31 00:00:00.000000','澶╂触','鍛 ','male','娑 ','璁＄畻鏈虹瀛︿笌鎶 鏈 ','Masses','璁＄畻鏈虹瀛︿笌宸ョ▼瀛﹂櫌','inSchool','11021108'),(213210009,'2003-12-31 00:00:00.000000','閲嶅簡','鍚 ','female','闈 ','涓村簥鍖诲','CommunistPartyOfChina','鍖诲闄 ','inSchool','11021109'),(213210010,'2003-12-31 00:00:00.000000','鍥涘窛','璧 ','male','瀛愯僵','寤虹瓚瀛 ','Masses','寤虹瓚瀛﹂櫌','inSchool','11021110'),(213210011,'2003-12-31 00:00:00.000000','姹熻嫃','鍛 ','female','闆呯惔','浜哄伐鏅鸿兘','CommunistPartyOfChina','浜哄伐鏅鸿兘瀛﹂櫌','inSchool','11021111'),(213210012,'2003-12-31 00:00:00.000000','娴欐睙','鍚 ','female','浜戝ぉ','杞欢宸ョ▼','Masses','杞欢瀛﹂櫌','inSchool','11021112'),(213210013,'2003-12-31 00:00:00.000000','娴峰崡','閮 ','male','鎬濇簮','缃戠粶绌洪棿瀹夊叏','CommunistPartyOfChina','缃戠粶绌洪棿瀹夊叏瀛﹂櫌','inSchool','11021113'),(213210014,'2003-12-31 00:00:00.000000','绂忓缓','鐜 ','female','鐞 ','淇℃伅','Masses','鐢靛瓙淇℃伅瀛﹂櫌','inSchool','11021114'),(213210015,'2003-12-31 00:00:00.000000','姹熻タ','寮 ','male','杈 ','鍦熸湪宸ョ▼','CommunistPartyOfChina','浜ら 氬闄 ','inSchool','11021115');
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teaching_evaluation`
--

DROP TABLE IF EXISTS `teaching_evaluation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teaching_evaluation` (
  `uuid` binary(16) NOT NULL,
  `classUuid` binary(16) NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  `result` json DEFAULT NULL,
  `studentId` int NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teaching_evaluation`
--

LOCK TABLES `teaching_evaluation` WRITE;
/*!40000 ALTER TABLE `teaching_evaluation` DISABLE KEYS */;
/*!40000 ALTER TABLE `teaching_evaluation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `card_number` int NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` enum('female','male','unspecified') COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`card_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (123456,'admin@seu.edu.cn','unspecified','绠＄悊鍛 ','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','12345678901','admin,student,teacher,affairs_staff,library_user,library_staff,shop_user,shop_staff,finance_staff,finance_user,gpt_user'),(1234567,'lixiang@seu.edu.cn','male','閮棴','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13700000016','affairs_staff'),(12345678,'lixiang@seu.edu.cn','female','鏈遍洩鐟 ','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13900000000','teacher,library_user,shop_user,finance_user'),(109000601,'lixiang@seu.edu.cn','male','椹枃鏉 ','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13900000001','teacher,library_user,shop_user,finance_user'),(109000602,'lixiang@seu.edu.cn','male','楂樺北姘 ','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13900000002','teacher,library_user,shop_user,finance_user'),(109000603,'lixiang@seu.edu.cn','male','鐜嬫杞 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13900000003','teacher,library_user,shop_user,finance_user'),(109000604,'lixiang@seu.edu.cn','female','寮犵繝鑺 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13900000004','teacher,library_user,shop_user,finance_user'),(109000605,'lixiang@seu.edu.cn','male','闄堝織璞 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13900000005','teacher,library_user,shop_user,finance_user'),(109000606,'lixiang@seu.edu.cn','male','閮棴','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13900000006','teacher,library_user,shop_user,finance_user'),(123456789,'lixiang@seu.edu.cn','male','鏉庢兂','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13700000000','student,library_user,shop_user,finance_user'),(213210001,'lixiang@seu.edu.cn','male','鏉庡崕','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000001','student,library_user,shop_user,finance_user'),(213210002,'lixiang@seu.edu.cn','male','寮犱紵','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000002','student,library_user,shop_user,finance_user'),(213210003,'lixiang@seu.edu.cn','male','鏉庢 濇  ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000003','student,library_user,shop_user,finance_user'),(213210004,'lixiang@seu.edu.cn','male','寮犳枃杞 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000004','student,library_user,shop_user,finance_user'),(213210005,'lixiang@seu.edu.cn','female','鐜嬪ぉ涓 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000005','student,library_user,shop_user,finance_user'),(213210006,'lixiang@seu.edu.cn','male','鍒樻捣娲 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000006','student,library_user,shop_user,finance_user'),(213210007,'lixiang@seu.edu.cn','male','闄堝皬鏄 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000007','student,library_user,shop_user,finance_user'),(213210008,'lixiang@seu.edu.cn','male','鍛ㄦ稕','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000008','student,library_user,shop_user,finance_user'),(213210009,'lixiang@seu.edu.cn','female','鍚撮潤','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000009','student,library_user,shop_user,finance_user'),(213210010,'lixiang@seu.edu.cn','male','璧靛瓙杞 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000010','student,library_user,shop_user,finance_user'),(213210011,'lixiang@seu.edu.cn','female','鍛ㄩ泤鐞 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000011','student,library_user,shop_user,finance_user'),(213210012,'lixiang@seu.edu.cn','female','鍚翠簯澶 ','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000012','student,library_user,shop_user,finance_user'),(213210013,'lixiang@seu.edu.cn','male','閮戞 濇簮','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000013','student,library_user,shop_user,finance_user'),(213210014,'lixiang@seu.edu.cn','female','鐜嬬惓','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000014','student,library_user,shop_user,finance_user'),(213210015,'lixiang@seu.edu.cn','male','寮犺緣','$argon2id$v=19$m=65536,t=3,p=4$nvf6WnuVadpLu9HmiqU/qA$5Ec4a6dyeDbnQj+hsA9i9E7EFnfgO2rOiDZMeugVyxg','13700000015','student,library_user,shop_user,finance_user');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-13 23:21:24
