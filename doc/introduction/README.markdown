# 文档编辑规范

## 目录结构

```bash
.
├── body
│   ├── database_design.tex
│   ├── introduction.tex
│   ├── modules
│   │   ├── common.tex
│   │   ├── multithreading.tex
│   │   ├── network.tex
│   │   └── user_management.tex
│   ├── program_system_analysis.tex
│   └── structure.tex
├── example
│   ├── codeblock.tex
│   └── table.tex
├── fig
├── header
│   ├── header.tex
│   ├── modification_history.tex
│   └── title_and_titlepage.tex
├── introduction.pdf
└── introduction.tex
```

请只在`/body`路径下编辑自己的部分。

## 其他

- 除 `introduction.tex`外，所有的`.tex`文件必须在文件开始的位置加入以下四行：

```latex
% !TeX root = ../introduction.tex
\ifx\maindoc\undefined
\errmessage{This file must be input from a main document!}
\fi
```

第一行的路径为`introduction.tex`相对于当前`.tex`文件的位置，对于`/body/modules`内的`.tex`文件，这个路径为`../../introduction.tex`

- 所有的文件命名都应该遵循 `snake_case`，即所有单词小写，单词之间用下划线 `_` 分隔

- 请确保所有的图片都在`/fig`下！每一个特定的 `.tex` 文件下所用到的图片都应当有**独一无二的文件名前缀**（推荐直接用自己所编辑文件的文件名）。
  比方说， `introduction.tex` 中插入的图片都应命名为 `introduction_xxxx.pdf/png` 

- 若要插入图片和代码块，请参考 `/example`内的文件！