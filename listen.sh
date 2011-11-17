#!/bin/sh
# run the script: ./listen.sh
# it will listen on port 65001 for incoming videos from a ShirtCam
# (osx only: if you want to use on linux/windows, dont "open" the video:
# `nc -l 65001 | pv > ${VIDEO_COUNT}.mp4`
# )

VIDEO_COUNT=0
while true
do
  VIDEO_COUNT+=1
  `nc -l 65001 | pv > ${VIDEO_COUNT}.mp4 && open ${VIDEO_COUNT}.mp4`
done
