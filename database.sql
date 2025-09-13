-- MySQL dump 10.13  Distrib 8.4.6, for macos15 (arm64)
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
INSERT INTO `book` VALUES (_binary '	Qa�I��\�','余华','lend','I247.5993','http://img3m4.ddimg.cn/6/32/663823914-1_w_3.jpg','《活着》讲述了在大时代背景下，随着内战、三反五反、大跃进、“文化大革命”等社会变革，徐福贵的人生和家庭不断经受着苦难，到了最后所有亲人都先后离他而去，仅剩下年老的他和一头老牛相依为命。小说以普通、平实的故事情节讲述了在急剧变革的时代中福贵的不幸遭遇和坎坷命运，在冷静的笔触中展现了生命的意义和存在的价值，揭示了命运的无奈，与生活的不可捉摸。','9787506365390','活着','李文正图书馆中文图书阅览室','作家出版社'),(_binary '>�EWF�$I\�','刘瑜','newly','D0/639','https://i.dawnlab.me/935400df71a34577a43e116bc6c361e6.jpg','一个和平“爆发”的年代？ 历史“终结论”的终结？ 国家能力从何而来？ 文明的冲突是一个过时的预言？ …… 面对林林总总的政治问题，作者带领我们以一种比较的视角，在民主问责和国家能力两个政治比较的核心维度下，建立起观察的参照系，将不同体制、不同经济发展水平的国家纳入比较的视野，去分析我们的时代背景和全球化进程，讨论不同国家的政治转型与国家能力，以及文化和经济对政治变迁的影响。 “政治是可能性的艺术。”当我们将面对的政治现实当作一万种可能性之一来对待时，就能从此时此地抽离，获得一种俯瞰的视角，进而再聚焦定位现实，在浩瀚的可能性中理解我们自身。 「名人推荐」 比较政治学当中的“比较”，与其说是一种具体的研究方法，不如说是一种研究的视野。当你把你所面对的政治现实当作一万种可能性之一来对待时，你就获得了一种“比较的视野”。 比较的视野本质上是一种... (展开全部) 「内容简介」 一个和平“爆发”的年代？ 历史“终结论”的终结？ 国家能力从何而来？ 文明的冲突是一个过时的预言？ …… 面对林林总总的政治问题，作者带领我们以一种比较的视角，在民主问责和国家能力两个政治比较的核心维度下，建立起观察的参照系，将不同体制、不同经济发展水平的国家纳入比较的视野，去分析我们的时代背景和全球化进程，讨论不同国家的政治转型与国家能力，以及文化和经济对政治变迁的影响。 “政治是可能性的艺术。”当我们将面对的政治现实当作一万种可能性之一来对待时，就能从此时此地抽离，获得一种俯瞰的视角，进而再聚焦定位现实，在浩瀚的可能性中理解我们自身。','9787559848048','可能性的艺术：比较政治学30讲','中文图书阅览室1（九龙湖A208）','广西师范大学出版社'),(_binary 'OqGbHl�1d޿','北岛','available','I217.02/1471','https://i.dawnlab.me/e23c468d53e7c4fa5846bd2fac402ae1.png','《必有人重写爱情》是著名诗人北岛的作品精选集，46篇经典诗歌与散文作品，呈现诗人、散文家、画者、摄影者北岛的创作全貌。“一诗一文”的编排特色，如同中国诗话传统之传续。从“暴风雨的记忆”到“词的流亡”到“大地之书”，三辑诗文作品完整展现诗人自童年至今的人生行旅，映照二十世纪下半叶激荡的时代风景。 “我受雇于一个伟大的记忆。”北岛的诗歌是一代人的集体记忆与精神镜像。自我与时代，他乡与故乡，历史与现实，在种种悖论与断裂中，在出发与抵达之间，北岛把词语垒进历史。北岛的散文则简净有妙趣，从艾伦•金斯堡的纽约到卡夫卡的布拉格，从阿拉法特的拉马拉到洛尔迦的西班牙，他在漂泊的世界行旅中与身份各异的诗人学者等相逢相际，所见所闻鲜活亦惊心；也忆少年往事、亲友挚交，浓烈乡愁与尖锐疼痛常不期而至。诗人之笔写作散文，恢复汉语的丰富、敏锐、新鲜。书中亦有多幅北岛的绘... (展开全部) 内容简介： 《必有人重写爱情》是著名诗人北岛的作品精选集，46篇经典诗歌与散文作品，呈现诗人、散文家、画者、摄影者北岛的创作全貌。“一诗一文”的编排特色，如同中国诗话传统之传续。从“暴风雨的记忆”到“词的流亡”到“大地之书”，三辑诗文作品完整展现诗人自童年至今的人生行旅，映照二十世纪下半叶激荡的时代风景。 “我受雇于一个伟大的记忆。”北岛的诗歌是一代人的集体记忆与精神镜像。自我与时代，他乡与故乡，历史与现实，在种种悖论与断裂中，在出发与抵达之间，北岛把词语垒进历史。北岛的散文则简净有妙趣，从艾伦•金斯堡的纽约到卡夫卡的布拉格，从阿拉法特的拉马拉到洛尔迦的西班牙，他在漂泊的世界行旅中与身份各异的诗人学者等相逢相际，所见所闻鲜活亦惊心；也忆少年往事、亲友挚交，浓烈乡愁与尖锐疼痛常不期而至。诗人之笔写作散文，恢复汉语的丰富、敏锐、新鲜。书中亦有多幅北岛的绘画与摄影作品，这是他在文字之外找寻到的另一种语言。','9787544398565','必有人重写爱情','中文图书阅览室1（九龙湖A208）','海南出版社'),(_binary ' ���\'C�','刘瑜','available','D0/639','https://i.dawnlab.me/935400df71a34577a43e116bc6c361e6.jpg','一个和平“爆发”的年代？ 历史“终结论”的终结？ 国家能力从何而来？ 文明的冲突是一个过时的预言？ …… 面对林林总总的政治问题，作者带领我们以一种比较的视角，在民主问责和国家能力两个政治比较的核心维度下，建立起观察的参照系，将不同体制、不同经济发展水平的国家纳入比较的视野，去分析我们的时代背景和全球化进程，讨论不同国家的政治转型与国家能力，以及文化和经济对政治变迁的影响。 “政治是可能性的艺术。”当我们将面对的政治现实当作一万种可能性之一来对待时，就能从此时此地抽离，获得一种俯瞰的视角，进而再聚焦定位现实，在浩瀚的可能性中理解我们自身。 「名人推荐」 比较政治学当中的“比较”，与其说是一种具体的研究方法，不如说是一种研究的视野。当你把你所面对的政治现实当作一万种可能性之一来对待时，你就获得了一种“比较的视野”。 比较的视野本质上是一种... (展开全部) 「内容简介」 一个和平“爆发”的年代？ 历史“终结论”的终结？ 国家能力从何而来？ 文明的冲突是一个过时的预言？ …… 面对林林总总的政治问题，作者带领我们以一种比较的视角，在民主问责和国家能力两个政治比较的核心维度下，建立起观察的参照系，将不同体制、不同经济发展水平的国家纳入比较的视野，去分析我们的时代背景和全球化进程，讨论不同国家的政治转型与国家能力，以及文化和经济对政治变迁的影响。 “政治是可能性的艺术。”当我们将面对的政治现实当作一万种可能性之一来对待时，就能从此时此地抽离，获得一种俯瞰的视角，进而再聚焦定位现实，在浩瀚的可能性中理解我们自身。','9787559848048','可能性的艺术：比较政治学30讲','中文图书阅览室1（九龙湖A208）','广西师范大学出版社'),(_binary '7f�q\0%N��h','刘瑜','lend','D0/639','https://i.dawnlab.me/935400df71a34577a43e116bc6c361e6.jpg','一个和平“爆发”的年代？ 历史“终结论”的终结？ 国家能力从何而来？ 文明的冲突是一个过时的预言？ …… 面对林林总总的政治问题，作者带领我们以一种比较的视角，在民主问责和国家能力两个政治比较的核心维度下，建立起观察的参照系，将不同体制、不同经济发展水平的国家纳入比较的视野，去分析我们的时代背景和全球化进程，讨论不同国家的政治转型与国家能力，以及文化和经济对政治变迁的影响。 “政治是可能性的艺术。”当我们将面对的政治现实当作一万种可能性之一来对待时，就能从此时此地抽离，获得一种俯瞰的视角，进而再聚焦定位现实，在浩瀚的可能性中理解我们自身。 「名人推荐」 比较政治学当中的“比较”，与其说是一种具体的研究方法，不如说是一种研究的视野。当你把你所面对的政治现实当作一万种可能性之一来对待时，你就获得了一种“比较的视野”。 比较的视野本质上是一种... (展开全部) 「内容简介」 一个和平“爆发”的年代？ 历史“终结论”的终结？ 国家能力从何而来？ 文明的冲突是一个过时的预言？ …… 面对林林总总的政治问题，作者带领我们以一种比较的视角，在民主问责和国家能力两个政治比较的核心维度下，建立起观察的参照系，将不同体制、不同经济发展水平的国家纳入比较的视野，去分析我们的时代背景和全球化进程，讨论不同国家的政治转型与国家能力，以及文化和经济对政治变迁的影响。 “政治是可能性的艺术。”当我们将面对的政治现实当作一万种可能性之一来对待时，就能从此时此地抽离，获得一种俯瞰的视角，进而再聚焦定位现实，在浩瀚的可能性中理解我们自身。','9787559848048','可能性的艺术：比较政治学30讲','中文图书阅览室1（九龙湖A208）','广西师范大学出版社'),(_binary ';읲��@D�','刘瑜','newly','D0/639','https://i.dawnlab.me/935400df71a34577a43e116bc6c361e6.jpg','一个和平“爆发”的年代？ 历史“终结论”的终结？ 国家能力从何而来？ 文明的冲突是一个过时的预言？ …… 面对林林总总的政治问题，作者带领我们以一种比较的视角，在民主问责和国家能力两个政治比较的核心维度下，建立起观察的参照系，将不同体制、不同经济发展水平的国家纳入比较的视野，去分析我们的时代背景和全球化进程，讨论不同国家的政治转型与国家能力，以及文化和经济对政治变迁的影响。 “政治是可能性的艺术。”当我们将面对的政治现实当作一万种可能性之一来对待时，就能从此时此地抽离，获得一种俯瞰的视角，进而再聚焦定位现实，在浩瀚的可能性中理解我们自身。 「名人推荐」 比较政治学当中的“比较”，与其说是一种具体的研究方法，不如说是一种研究的视野。当你把你所面对的政治现实当作一万种可能性之一来对待时，你就获得了一种“比较的视野”。 比较的视野本质上是一种... (展开全部) 「内容简介」 一个和平“爆发”的年代？ 历史“终结论”的终结？ 国家能力从何而来？ 文明的冲突是一个过时的预言？ …… 面对林林总总的政治问题，作者带领我们以一种比较的视角，在民主问责和国家能力两个政治比较的核心维度下，建立起观察的参照系，将不同体制、不同经济发展水平的国家纳入比较的视野，去分析我们的时代背景和全球化进程，讨论不同国家的政治转型与国家能力，以及文化和经济对政治变迁的影响。 “政治是可能性的艺术。”当我们将面对的政治现实当作一万种可能性之一来对待时，就能从此时此地抽离，获得一种俯瞰的视角，进而再聚焦定位现实，在浩瀚的可能性中理解我们自身。','9787559848048','可能性的艺术：比较政治学30讲','中文图书阅览室1（九龙湖A208）','广西师范大学出版社'),(_binary '}����M\�','北岛','available','I217.02/1471','https://i.dawnlab.me/e23c468d53e7c4fa5846bd2fac402ae1.png','《必有人重写爱情》是著名诗人北岛的作品精选集，46篇经典诗歌与散文作品，呈现诗人、散文家、画者、摄影者北岛的创作全貌。“一诗一文”的编排特色，如同中国诗话传统之传续。从“暴风雨的记忆”到“词的流亡”到“大地之书”，三辑诗文作品完整展现诗人自童年至今的人生行旅，映照二十世纪下半叶激荡的时代风景。 “我受雇于一个伟大的记忆。”北岛的诗歌是一代人的集体记忆与精神镜像。自我与时代，他乡与故乡，历史与现实，在种种悖论与断裂中，在出发与抵达之间，北岛把词语垒进历史。北岛的散文则简净有妙趣，从艾伦•金斯堡的纽约到卡夫卡的布拉格，从阿拉法特的拉马拉到洛尔迦的西班牙，他在漂泊的世界行旅中与身份各异的诗人学者等相逢相际，所见所闻鲜活亦惊心；也忆少年往事、亲友挚交，浓烈乡愁与尖锐疼痛常不期而至。诗人之笔写作散文，恢复汉语的丰富、敏锐、新鲜。书中亦有多幅北岛的绘... (展开全部) 内容简介： 《必有人重写爱情》是著名诗人北岛的作品精选集，46篇经典诗歌与散文作品，呈现诗人、散文家、画者、摄影者北岛的创作全貌。“一诗一文”的编排特色，如同中国诗话传统之传续。从“暴风雨的记忆”到“词的流亡”到“大地之书”，三辑诗文作品完整展现诗人自童年至今的人生行旅，映照二十世纪下半叶激荡的时代风景。 “我受雇于一个伟大的记忆。”北岛的诗歌是一代人的集体记忆与精神镜像。自我与时代，他乡与故乡，历史与现实，在种种悖论与断裂中，在出发与抵达之间，北岛把词语垒进历史。北岛的散文则简净有妙趣，从艾伦•金斯堡的纽约到卡夫卡的布拉格，从阿拉法特的拉马拉到洛尔迦的西班牙，他在漂泊的世界行旅中与身份各异的诗人学者等相逢相际，所见所闻鲜活亦惊心；也忆少年往事、亲友挚交，浓烈乡愁与尖锐疼痛常不期而至。诗人之笔写作散文，恢复汉语的丰富、敏锐、新鲜。书中亦有多幅北岛的绘画与摄影作品，这是他在文字之外找寻到的另一种语言。','9787544398565','必有人重写爱情','中文图书阅览室1（九龙湖A208）','海南出版社'),(_binary '�~��{8C[\�','伯德,舍温','available','K837.126.1/30','https://i.dawnlab.me/8f17fc565e3871bf8ca9ad22da33c3e5.png','《奥本海默传》是美国“原子弹之父”罗伯特·J.奥本海默的第一部完整传记，曾荣获2006年美国普利策奖（传记类）。作为一名杰出的且富于魅力的物理学家，奥本海默在第二次世界大战期间为美国贡献了一切。原子弹在日本广岛爆炸之后，奥本海默成为同代人中最著名的科学家，同时也是20世纪最具争议性的人物之一，并成为面对科学进步的现代人的形象化身。 奥本海默是对核物质实施国际控制的激进提案的发起者之一，这一思想即使在今天也是至关重要的。他极力反对美国发展氢弹，并强烈反对美国空军试图发动一场极其危险的核战争的计划。20世纪50年代初是一个充满癔症的年代，奥本海默的思想自然就成为强力支持建造大规模杀伤性武器的人们诅咒的对象。作为回应，美国原子能委员会主席刘易斯·斯特劳斯、超级核弹的支持者爱德华·特勒，以及美国联邦调查局局长埃德加·胡佛在幕后精心策划了一场安全听证会，从而切断了奥本海默与美国核秘密的联系。 《奥本海默传》以前所未有的细节揭示了奥本海默的生活及其所处的时代。作者通过从美国国内和国外的资料馆搜集来的大量记录和信件、美国联邦调查局的档案，以及与奥本海默最亲近的朋友、亲戚和同事的访谈，对奥本海默的一生作了详尽无遗的研究。20世纪初，奥本海默在纽约市的道德文化学校接受了早期教育，之后在哈佛大学和剑桥大学遇到了他人生的第一次个人危机。于是他去了德国，在那里，他跟世界上最有成就的理论家们学习了量子物理学。而后，他在美国加利福尼亚的伯克利建立了国内一流的理论物理学院。在那里，他还深深地卷入了社会公正事业，并与很多共产党人保持着联系。后来，他去了新墨西哥州的洛斯阿拉莫斯，并在那里建立了世界上最具潜力的核武器实验室；与此同时，他也改变了自己。1947年至1966年，他一直担任美国普林斯顿高等研究院的主管一职。 《奥本海默传》生动勾画出了一位杰出的、充满抱负的、复杂的、有缺点的人物形象，他与美国的“大萧条”、第二次世界大战和冷战有着深刻的关联。它不但是一部充满历史感的人物传记，而且对于我们理解当下以及未来的选择都会具有重要的启示。','9787544710008','奥本海默传','九龙湖李文正图书馆','译林出版社'),(_binary '��	���','伯德,舍温','available','K837.126.1/30','https://i.dawnlab.me/8f17fc565e3871bf8ca9ad22da33c3e5.png','《奥本海默传》是美国“原子弹之父”罗伯特·J.奥本海默的第一部完整传记，曾荣获2006年美国普利策奖（传记类）。作为一名杰出的且富于魅力的物理学家，奥本海默在第二次世界大战期间为美国贡献了一切。原子弹在日本广岛爆炸之后，奥本海默成为同代人中最著名的科学家，同时也是20世纪最具争议性的人物之一，并成为面对科学进步的现代人的形象化身。 奥本海默是对核物质实施国际控制的激进提案的发起者之一，这一思想即使在今天也是至关重要的。他极力反对美国发展氢弹，并强烈反对美国空军试图发动一场极其危险的核战争的计划。20世纪50年代初是一个充满癔症的年代，奥本海默的思想自然就成为强力支持建造大规模杀伤性武器的人们诅咒的对象。作为回应，美国原子能委员会主席刘易斯·斯特劳斯、超级核弹的支持者爱德华·特勒，以及美国联邦调查局局长埃德加·胡佛在幕后精心策划了一场安全听证会，从而切断了奥本海默与美国核秘密的联系。 《奥本海默传》以前所未有的细节揭示了奥本海默的生活及其所处的时代。作者通过从美国国内和国外的资料馆搜集来的大量记录和信件、美国联邦调查局的档案，以及与奥本海默最亲近的朋友、亲戚和同事的访谈，对奥本海默的一生作了详尽无遗的研究。20世纪初，奥本海默在纽约市的道德文化学校接受了早期教育，之后在哈佛大学和剑桥大学遇到了他人生的第一次个人危机。于是他去了德国，在那里，他跟世界上最有成就的理论家们学习了量子物理学。而后，他在美国加利福尼亚的伯克利建立了国内一流的理论物理学院。在那里，他还深深地卷入了社会公正事业，并与很多共产党人保持着联系。后来，他去了新墨西哥州的洛斯阿拉莫斯，并在那里建立了世界上最具潜力的核武器实验室；与此同时，他也改变了自己。1947年至1966年，他一直担任美国普林斯顿高等研究院的主管一职。 《奥本海默传》生动勾画出了一位杰出的、充满抱负的、复杂的、有缺点的人物形象，他与美国的“大萧条”、第二次世界大战和冷战有着深刻的关联。它不但是一部充满历史感的人物传记，而且对于我们理解当下以及未来的选择都会具有重要的启示。','9787544710008','奥本海默传','九龙湖李文正图书馆','译林出版社');
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
INSERT INTO `card_transaction` VALUES (_binary 's�\�RFP�\�{#\�Tǹ',24900,123456,'2025-09-10 09:22:00.699000','payment','商店消费'),(_binary '\��L\�MC��]��\�Z\�',1170300,123456,'2025-09-10 09:24:00.695000','payment','商店消费'),(_binary 'v�ϧO��\�',100000,213210000,'2023-09-11 22:46:35.888000','deposit','充值'),(_binary '�\0�I�\�',2028950,123456,'2023-09-15 03:24:38.503000','payment','商店消费'),(_binary '�/}��M�',1269900,123456,'2023-09-15 08:16:16.785000','payment','商店消费'),(_binary '*�����',1294800,123456,'2023-09-15 08:28:38.118000','payment','商店消费'),(_binary '0т��uGT�\�',0,123456,'2023-09-14 23:28:54.640000','payment','商店消费'),(_binary '1����C\�',157700,123456,'2023-09-12 13:00:53.842000','payment','商店消费'),(_binary '<|��C��',24900,123456,'2023-09-15 08:16:05.103000','payment','商店消费'),(_binary 'Mg���.I�',13950,213210000,'2023-09-15 08:59:01.587000','payment','商店消费'),(_binary 'b�G���@',1359800,123456,'2023-09-14 23:32:24.237000','payment','商店消费'),(_binary 'e+�OFM�\�',2639700,123456,'2023-09-15 00:22:12.784000','payment','商店消费'),(_binary 'h�&n`EۺIr\r\�',72900,123456,'2023-09-14 16:17:49.292000','payment','商店消费'),(_binary '�O\"�\�@n��.�\�',0,123456,'2025-09-10 09:26:40.455000','payment','商店消费'),(_binary '��\�·D���$}��\�',1234567800,123456,'2025-09-10 10:40:59.878000','deposit','充值'),(_binary 'Ր�3��O`\�',199800,123456,'2023-09-15 08:16:08.471000','payment','商店消费'),(_binary '��|3PK~�\�',100000000,123456,'2023-09-14 23:35:24.627000','deposit','充值'),(_binary '�+�A�C \�',1000000,123456,'2023-09-14 23:32:18.517000','deposit','充值'),(_binary '�V�1�oG$\�',1269900,123456,'2023-09-15 01:12:45.541000','payment','商店消费'),(_binary '�]eS~_Hv�e\�',1269900,123456,'2023-09-15 08:16:29.661000','payment','商店消费'),(_binary '����B',165400,123456,'2023-09-13 09:11:02.755000','payment','商店消费'),(_binary '��(r�CߝT',165400,123456,'2023-09-11 22:37:34.293000','payment','商店消费'),(_binary '��5��M\�',1000000,123456,'2023-09-11 22:37:25.676000','deposit','充值'),(_binary '�����',1269900,123456,'2023-09-15 08:16:44.201000','payment','商店消费');
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
INSERT INTO `class` VALUES (_binary 'B�T���J\�',60,_binary '�W|�@�\�','教三-104','[{\"first\": {\"first\": 9, \"second\": 16}, \"second\": {\"first\": 2, \"second\": {\"first\": 11, \"second\": 12}}}, {\"first\": {\"first\": 9, \"second\": 16}, \"second\": {\"first\": 4, \"second\": {\"first\": 3, \"second\": 4}}}]',109000600),(_binary 'Mw�U�Mf�\�',60,_binary '�.��K|B\�','金智楼','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 2, \"second\": {\"first\": 1, \"second\": 4}}}, {\"first\": {\"first\": 4, \"second\": 8}, \"second\": {\"first\": 5, \"second\": {\"first\": 6, \"second\": 8}}}]',109000600),(_binary 'i�><�)@ږh=',60,_binary '���|dK�','教四-301','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 4, \"second\": {\"first\": 3, \"second\": 5}}}]',109000600),(_binary 'kF�\n�G�\�',15,_binary '����]@{\�','教八 201','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 1, \"second\": {\"first\": 1, \"second\": 4}}}]',109000601),(_binary 'r����I\�',60,_binary '�JP�>JBE�','教三-104','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 2, \"second\": {\"first\": 6, \"second\": 7}}}]',109000604),(_binary '�\r-W��Dq\�',30,_binary '�8jd�\rD\"�{','东九楼','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 1, \"second\": {\"first\": 1, \"second\": 2}}}]',123456),(_binary '��Dٮ:II�9',20,_binary '����]@{\�','教八 303','[{\"first\": {\"first\": 1, \"second\": 16}, \"second\": {\"first\": 3, \"second\": {\"first\": 1, \"second\": 4}}}]',109000602);
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
INSERT INTO `course` VALUES (_binary '�W|�@�\�','B09H1040','操作系统专题实践',4,'计算机科学与工程学院'),(_binary '�.��K|B\�','BJSL0456','专业技能实训',3,'计算机科学与工程学院'),(_binary '�8jd�\rD\"�{','BJSL0081','软件工程',4,'计算机科学与工程学院'),(_binary '�JP�>JBE�','B71S0030','编译原理',4,'计算机科学与工程学院'),(_binary '���|dK�','B09T1070','计算机与社会',2,'计算机科学与工程学院'),(_binary '����]@{\�','BJSL0123','Kotlin 101',4,'计算机科学与工程学院');
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
INSERT INTO `library_transaction` VALUES (_binary 'l���M؁\�',_binary '��	���','2023-09-15 08:57:50.819000','2023-09-15 08:57:58.937000',213210000,'2023-10-15 08:57:50.819000'),(_binary 'lR��Hކ_!',_binary '7f�q\0%N��h','2023-09-15 08:55:58.701000',NULL,213210000,'2023-10-15 08:55:58.701000');
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
INSERT INTO `select_record` VALUES (_binary '7@J�=L2�\�',213210000,_binary 'Mw�U�Mf�\�',NULL,'2023-09-15 08:27:06.373000'),(_binary 'Ti\n�O&��',213210004,_binary '��Dٮ:II�9',NULL,'2023-09-15 08:39:20.309000'),(_binary '�g�F�\�',213210012,_binary 'Mw�U�Mf�\�',NULL,'2023-09-15 08:40:08.592000'),(_binary '$�hh�O!�\�',213210003,_binary 'i�><�)@ږh=',NULL,'2023-09-15 08:38:36.651000'),(_binary '%�\n��C{\�',213210012,_binary 'B�T���J\�',NULL,'2023-09-15 08:40:14.161000'),(_binary 'M��%]Mԉ|u\�',213210003,_binary 'Mw�U�Mf�\�',NULL,'2023-09-15 08:38:31.159000'),(_binary 'NBgE�J̏EY\�',213210008,_binary 'r����I\�',NULL,'2023-09-15 08:39:48.488000'),(_binary 'P��]�dE�',213210003,_binary '��Dٮ:II�9',NULL,'2023-09-15 08:38:42.265000'),(_binary 'S<�P�I�\�',213210003,_binary '�\r-W��Dq\�',NULL,'2023-09-15 08:38:35.119000'),(_binary 'U3\Z�G��',123456,_binary '��Dٮ:II�9',NULL,'2023-09-15 01:17:45.437000'),(_binary ']���G�',213210004,_binary 'r����I\�',NULL,'2023-09-15 08:39:16.860000'),(_binary 'e�T��Iۼ\�',213210004,_binary '�\r-W��Dq\�',NULL,'2023-09-15 08:39:15.574000'),(_binary 'fv�,OG*��',213210004,_binary 'B�T���J\�',NULL,'2023-09-15 08:39:12.835000'),(_binary 'hy�	��E<\�',213210000,_binary '�\r-W��Dq\�',NULL,'2023-09-15 08:27:03.828000'),(_binary 'x���J¥\r',213210008,_binary 'kF�\n�G�\�',NULL,'2023-09-15 08:39:45.721000'),(_binary 'ݼe�-�@U�\r',213210004,_binary 'Mw�U�Mf�\�',NULL,'2023-09-15 08:39:10.098000'),(_binary '�fB\\�4[',213210012,_binary '��Dٮ:II�9',NULL,'2023-09-15 08:40:28.664000'),(_binary '�)r�L�\�',213210003,_binary 'r����I\�',NULL,'2023-09-15 08:38:38.486000'),(_binary '�%�M\Z�H�',213210000,_binary 'B�T���J\�',NULL,'2023-09-15 08:26:59.506000'),(_binary '�)l��AC�',213210008,_binary 'Mw�U�Mf�\�',NULL,'2023-09-15 08:39:39.374000'),(_binary '�X���QC\�',213210012,_binary 'r����I\�',NULL,'2023-09-15 08:40:17.860000'),(_binary '�_7�ۃM!�b',123456,_binary 'B�T���J\�',NULL,'2023-09-15 03:22:34.196000'),(_binary '�n~��(@L\�',213210012,_binary '�\r-W��Dq\�',NULL,'2023-09-15 08:40:25.677000'),(_binary '�t���D\�',123456,_binary '�\r-W��Dq\�',NULL,'2023-09-15 01:17:41.798000'),(_binary '�Ĩ?N��\�',213210008,_binary 'i�><�)@ږh=',NULL,'2023-09-15 08:39:41.329000'),(_binary '�����\�',213210000,_binary 'r����I\�',NULL,'2023-09-15 08:27:11.810000');
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
INSERT INTO `store_item` VALUES (_binary '\Z�.�L�\�','01234567891335','赞美太阳！','正版 黑暗之魂官方艺术设定集全套1-2-3册 DARK SOULS','https://i.dawnlab.me/864b843fd26967485f02c0628911531f.png',24900,50,0),(_binary '=�;���Ow','154442959971','专业录音麦克风','Sony/索尼 ECM-G1 枪型麦克风 大尺寸收音单元 清晰人声收录\n','https://i.dawnlab.me/633e9d5e7835e1f00aa7d5506d119ac6.png',99900,3,41),(_binary '@f�H��B \�','4571367890123','MBA','Apple/苹果 13 英寸 MacBook Air Apple M2 芯片 8 核中央处理器 8 核图形处理器 8GB 统一内存 256GB 固态硬盘','https://i.dawnlab.me/b214883312422e9a7149422c19d6ebd5.png',1269900,6,47),(_binary 'Rw o|	@#��7','32145540123','c++圣经！！','C++Primer中文版 第五版 计算机开发c语言从入门到精通','https://i.dawnlab.me/b5b1080d399f9faf04c8c78a02ec0eab.png',6250,2,93),(_binary 'z\'���G�\�','100042959971','吹风机测试','米家 小米电吹风 吹风机 负离子护发可折叠 大风力速干 H101 白色','https://i.dawnlab.me/269e038d982f78bb02c3e4ba3a06f8c9.png',7700,2,29),(_binary '�{�E��Lw','3214567890123','感觉不如原神','【原神官方/全款】刻晴·霆霓快雨Ver.1/7手办Genshin','https://i.dawnlab.me/520ff42fc04fa57487b6fb65ca277857.png',84800,1,96),(_binary '�ŧ�?GڲCa\�','0123456789122','原神，启动！','【原神官方/全款】甘雨·循循守月Ver.1/7手办 Genshin','https://i.dawnlab.me/654dfbf1dac9573d5fee4b0a8feb0f18.png',72900,60,40),(_binary '�ɹ�RwG5�','9874567890123','中国之光！遥遥领先！','HUAWEI Mate 60 Pro 12GB+1TB 雅川青','https://i.dawnlab.me/f7f7ad266921f1eb4fa25c8dceb10842.png',679900,1,17);
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
INSERT INTO `store_transaction` VALUES (_binary '%٢��H}�\�',1,123456,72900,_binary '�ŧ�?GڲCa\�','','2023-09-12 13:00:53.842000'),(_binary '�S�1ZEB�\�',1,123456,72900,_binary '�ŧ�?GڲCa\�','','2023-09-14 16:17:49.286000'),(_binary '*#2X��AF�,',1,123456,72900,_binary '�ŧ�?GڲCa\�','','2023-09-11 22:37:34.292000'),(_binary '.et�?�AW�F',1,123456,1269900,_binary '@f�H��B \�','','2023-09-15 08:16:16.785000'),(_binary '1��ԯ\�Bk��>z+\�t\�',1,123456,24900,_binary '\Z�.�L�\�','','2025-09-10 09:22:00.689000'),(_binary 'J�+\Z&7Ei�\Z\�',1,123456,84800,_binary '�{�E��Lw','','2023-09-12 13:00:53.828000'),(_binary '_��zgFKe�\�',1,123456,1269900,_binary '@f�H��B \�','','2023-09-15 01:12:45.533000'),(_binary '`Xo�?�Ic�\�',1,123456,7700,_binary 'z\'���G�\�','','2023-09-11 22:37:34.278000'),(_binary 'ak�TmLe�de5',1,123456,24900,_binary '\Z�.�L�\�','','2023-09-15 08:16:05.093000'),(_binary 'lnDMM4��d',1,123456,84800,_binary '�{�E��Lw','','2023-09-13 09:11:02.747000'),(_binary 'o���/jG\�',1,213210000,7700,_binary 'z\'���G�\�','','2023-09-15 08:59:01.587000'),(_binary 's,�3F��\�',1,123456,84800,_binary '�{�E��Lw','','2023-09-11 22:37:34.285000'),(_binary 't|of2�@��9',1,123456,72900,_binary '�ŧ�?GڲCa\�','','2023-09-15 03:24:38.503000'),(_binary 'v;�gF��g',1,123456,72900,_binary '�ŧ�?GڲCa\�','','2023-09-13 09:11:02.754000'),(_binary '�q���O\�',1,123456,1269900,_binary '@f�H��B \�','','2023-09-15 03:24:38.493000'),(_binary '��\�gɷD��[78?-\��',47,123456,24900,_binary '\Z�.�L�\�','','2025-09-10 09:24:00.694000'),(_binary '��3g7H�\�',2,123456,1269900,_binary '@f�H��B \�','','2023-09-15 00:22:12.784000'),(_binary '�#\\f�G��',2,123456,679900,_binary '�ɹ�RwG5�','','2023-09-14 23:32:24.237000'),(_binary '�D�ztL*�U',1,123456,679900,_binary '�ɹ�RwG5�','','2023-09-15 03:24:38.500000'),(_binary '�Iܙ�0F�\�',1,123456,7700,_binary 'z\'���G�\�','','2023-09-13 09:11:02.739000'),(_binary '�h��lBA\�',2,123456,99900,_binary '=�;���Ow','','2023-09-15 08:16:08.471000'),(_binary '�k�7\rC`�^',1,123456,24900,_binary '\Z�.�L�\�','','2023-09-15 08:28:38.099000'),(_binary '�ot_B��\�',1,123456,1269900,_binary '@f�H��B \�','','2023-09-15 08:16:29.661000'),(_binary '�yіn�I�\�',1,213210000,6250,_binary 'Rw o|	@#��7','','2023-09-15 08:59:01.581000'),(_binary '�� �A�',1,123456,1269900,_binary '@f�H��B \�','','2023-09-15 08:16:44.201000'),(_binary '��\Z��Jr',1,123456,99900,_binary '=�;���Ow','','2023-09-15 00:22:12.775000'),(_binary '��A�\"�Ov',1,123456,1269900,_binary '@f�H��B \�','','2023-09-15 08:28:38.117000'),(_binary '����خ@%',1,123456,6250,_binary 'Rw o|	@#��7','','2023-09-15 03:24:38.498000');
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
INSERT INTO `student` VALUES (123456,'2003-12-31 00:00:00.000000','江苏','王','male','小明','计算机科学与技术','Masses','计算机科学与工程学院','graduated','09021200'),(213210000,'2003-12-31 00:00:00.000000','浙江','李','male','想','临床医学','CommunistPartyOfChina','医学院','inSchool','11021100'),(213210001,'2003-12-31 00:00:00.000000','浙江','李','male','华','临床医学','CommunistPartyOfChina','医学院','inSchool','11021101'),(213210002,'2003-12-31 00:00:00.000000','海南','张','male','伟','建筑学','Masses','建筑学院','inSchool','11021102'),(213210003,'2003-12-31 00:00:00.000000','福建','李','male','思思','人工智能','CommunistPartyOfChina','人工智能学院','inSchool','11021103'),(213210004,'2003-12-31 00:00:00.000000','江西','张','male','文轩','软件工程','Masses','软件学院','inSchool','11021104'),(213210005,'2003-12-31 00:00:00.000000','安徽','王','female','天一','网络空间安全','CommunistPartyOfChina','网络空间安全学院','inSchool','11021105'),(213210006,'2003-12-31 00:00:00.000000','北京','刘','male','海洋','信息','Masses','电子信息学院','inSchool','11021106'),(213210007,'2003-12-31 00:00:00.000000','上海','陈','male','小春','土木工程','CommunistPartyOfChina','交通学院','inSchool','11021107'),(213210008,'2003-12-31 00:00:00.000000','天津','周','male','涛','计算机科学与技术','Masses','计算机科学与工程学院','inSchool','11021108'),(213210009,'2003-12-31 00:00:00.000000','重庆','吴','female','静','临床医学','CommunistPartyOfChina','医学院','inSchool','11021109'),(213210010,'2003-12-31 00:00:00.000000','四川','赵','male','子轩','建筑学','Masses','建筑学院','inSchool','11021110'),(213210011,'2003-12-31 00:00:00.000000','江苏','周','female','雅琴','人工智能','CommunistPartyOfChina','人工智能学院','inSchool','11021111'),(213210012,'2003-12-31 00:00:00.000000','浙江','吴','female','云天','软件工程','Masses','软件学院','inSchool','11021112'),(213210013,'2003-12-31 00:00:00.000000','海南','郑','male','思源','网络空间安全','CommunistPartyOfChina','网络空间安全学院','inSchool','11021113'),(213210014,'2003-12-31 00:00:00.000000','福建','王','female','琳','信息','Masses','电子信息学院','inSchool','11021114'),(213210015,'2003-12-31 00:00:00.000000','江西','张','male','辉','土木工程','CommunistPartyOfChina','交通学院','inSchool','11021115');
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
INSERT INTO `user` VALUES (123456,'admin@seu.edu.cn','unspecified','管理员','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','12345678901','admin,student,teacher,affairs_staff,library_user,library_staff,shop_user,shop_staff,finance_staff,finance_user'),(1234567,'lixiang@seu.edu.cn','male','郭旭','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13700000016','affairs_staff'),(12345678,'lixiang@seu.edu.cn','female','朱雪瑶','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13900000000','teacher,library_user,shop_user,finance_user'),(109000601,'lixiang@seu.edu.cn','male','马文杰','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13900000001','teacher,library_user,shop_user,finance_user'),(109000602,'lixiang@seu.edu.cn','male','高山水','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13900000002','teacher,library_user,shop_user,finance_user'),(109000603,'lixiang@seu.edu.cn','male','王梓轩','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13900000003','teacher,library_user,shop_user,finance_user'),(109000604,'lixiang@seu.edu.cn','female','张翠花','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13900000004','teacher,library_user,shop_user,finance_user'),(109000605,'lixiang@seu.edu.cn','male','陈志豪','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13900000005','teacher,library_user,shop_user,finance_user'),(109000606,'lixiang@seu.edu.cn','male','郭旭','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13900000006','teacher,library_user,shop_user,finance_user'),(123456789,'lixiang@seu.edu.cn','male','李想','$argon2id$v=19$m=4096,t=3,p=1$7iutpJ+3/cQGyu+XEyTaNg$+Ho+5MD5NG3UpwbZCYqMl319wCmQlSdPV+ChFhD41Ok','13700000000','student,library_user,shop_user,finance_user'),(213210001,'lixiang@seu.edu.cn','male','李华','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000001','student,library_user,shop_user,finance_user'),(213210002,'lixiang@seu.edu.cn','male','张伟','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000002','student,library_user,shop_user,finance_user'),(213210003,'lixiang@seu.edu.cn','male','李思思','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000003','student,library_user,shop_user,finance_user'),(213210004,'lixiang@seu.edu.cn','male','张文轩','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000004','student,library_user,shop_user,finance_user'),(213210005,'lixiang@seu.edu.cn','female','王天一','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000005','student,library_user,shop_user,finance_user'),(213210006,'lixiang@seu.edu.cn','male','刘海洋','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000006','student,library_user,shop_user,finance_user'),(213210007,'lixiang@seu.edu.cn','male','陈小春','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000007','student,library_user,shop_user,finance_user'),(213210008,'lixiang@seu.edu.cn','male','周涛','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000008','student,library_user,shop_user,finance_user'),(213210009,'lixiang@seu.edu.cn','female','吴静','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000009','student,library_user,shop_user,finance_user'),(213210010,'lixiang@seu.edu.cn','male','赵子轩','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000010','student,library_user,shop_user,finance_user'),(213210011,'lixiang@seu.edu.cn','female','周雅琴','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000011','student,library_user,shop_user,finance_user'),(213210012,'lixiang@seu.edu.cn','female','吴云天','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000012','student,library_user,shop_user,finance_user'),(213210013,'lixiang@seu.edu.cn','male','郑思源','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000013','student,library_user,shop_user,finance_user'),(213210014,'lixiang@seu.edu.cn','female','王琳','$argon2id$v=19$m=4096,t=3,p=1$wUGLRxBzHTeREUuGWODTqA$u3xRa++L+THJ1TnYrTt/aQ0/jkEHrOh6Qhd9FB07qRQ','13700000014','student,library_user,shop_user,finance_user'),(213210015,'lixiang@seu.edu.cn','male','张辉','$argon2id$v=19$m=65536,t=3,p=4$nvf6WnuVadpLu9HmiqU/qA$5Ec4a6dyeDbnQj+hsA9i9E7EFnfgO2rOiDZMeugVyxg','13700000015','student,library_user,shop_user,finance_user');
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

-- Dump completed on 2025-09-13 19:42:57
