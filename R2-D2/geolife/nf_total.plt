#!/usr/bin/gnuplot

######## may modify #########
dataset="geolife"
test_name="nf"
file_name=''.dataset.'_'.test_name
#############################

reset
set terminal postscript eps color solid linewidth 2 "Helvetica" 34 size 6, 4
#set terminal pdfcairo lw 2 font "Helvetica, 20" size 6, 4
set output file_name."_total.eps"

set tmargin 1
set key on inside left top Left reverse font ",23" samplen 1.5 vertical maxrows 4 box width -4 spacing 0.8

set ylabel "Total Cost" font  "Arial, 34"
set xlabel "Number of Friends" font  "Arial, 34"

set xrange [0:4]
set xtics ("10" 0, "20" 1, "30" 2, "40" 3, "50" 4)

set yrange [0:20000000]
#set logscale y

unset label

plot '/home/xuying/proxim/4_txt_to_dat/pointdatagen_v2.0/'.dataset.'/dat/'.file_name.'.dat' \
   using ($1) title 'FMD'           with linespoints lt rgb "#4169E1"		ps 2 pt 7 lw 1,\
'' using ($2) title 'CMD'           with linespoints lt rgb "#800080"		ps 2 pt 6 lw 1,\
'' using ($3) title 'Stripe+RMF'	with linespoints lt rgb "#FFA500"		ps 2 pt 5 lw 1,\
'' using ($4) title 'Stripe+R2-D2'	with linespoints lt rgb "#8B4513"		ps 2 pt 4 lw 1,\
'' using ($5) title 'Stripe+HMM'    with linespoints lt rgb "#FF0000"		ps 2 pt 3 lw 1,\
'' using ($6) title 'Stripe+KF'     with linespoints lt rgb "#006400"		ps 2 pt 2 lw 1,\
#'' using ($7) title 'Stripe+OPT'    with linespoints lt rgb "#000000"		ps 2 pt 1 lw 1,

# black/darkgreen/red/saddlebrown/orange/purple/royalblue

###################################################

unset multiplot

exit
