#!/bin/bash

# 结束指定端口的进程脚本
# 使用方法: ./kill_port.sh <端口号>
# 例如: ./kill_port.sh 8080

# 检查参数
if [ $# -eq 0 ]; then
    echo "错误: 请指定要结束的端口号"
    echo "使用方法: $0 <端口号>"
    echo "例如: $0 8080"
    exit 1
fi

PORT=$1

# 验证端口号是否为数字
if ! [[ "$PORT" =~ ^[0-9]+$ ]]; then
    echo "错误: 端口号必须是数字"
    exit 1
fi

# 验证端口号范围
if [ "$PORT" -lt 1 ] || [ "$PORT" -gt 65535 ]; then
    echo "错误: 端口号必须在 1-65535 范围内"
    exit 1
fi

echo "正在查找占用端口 $PORT 的进程..."

# 查找占用指定端口的进程详细信息
PROCESS_INFO=$(lsof -i :$PORT 2>/dev/null)

if [ -z "$PROCESS_INFO" ]; then
    echo "没有找到占用端口 $PORT 的进程"
    exit 0
fi

echo "找到以下进程占用端口 $PORT:"
echo "$PROCESS_INFO"
echo ""

# 获取所有PID
PIDS=($(echo "$PROCESS_INFO" | awk 'NR>1 {print $2}' | sort -u))

if [ ${#PIDS[@]} -eq 1 ]; then
    # 只有一个进程
    PID=${PIDS[0]}
    PROCESS_NAME=$(echo "$PROCESS_INFO" | awk -v pid="$PID" '$2==pid {print $1}' | head -1)
    read -p "确认要结束进程 $PID ($PROCESS_NAME) 吗? (y/N): " -n 1 -r
    echo ""

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        SELECTED_PID=$PID
    else
        echo "操作已取消"
        exit 0
    fi
else
    # 多个进程，让用户选择
    echo "发现多个进程，请选择要结束的进程:"
    echo ""

    for i in "${!PIDS[@]}"; do
        PID=${PIDS[$i]}
        PROCESS_NAME=$(echo "$PROCESS_INFO" | awk -v pid="$PID" '$2==pid {print $1}' | head -1)
        USER=$(echo "$PROCESS_INFO" | awk -v pid="$PID" '$2==pid {print $3}' | head -1)
        echo "$((i+1)). PID: $PID, 进程: $PROCESS_NAME, 用户: $USER"
    done

    echo "$((${#PIDS[@]}+1)). 结束所有进程"
    echo "0. 取消操作"
    echo ""

    while true; do
        read -p "请选择 (0-$((${#PIDS[@]}+1))): " choice

        if [[ "$choice" =~ ^[0-9]+$ ]]; then
            if [ "$choice" -eq 0 ]; then
                echo "操作已取消"
                exit 0
            elif [ "$choice" -eq $((${#PIDS[@]}+1)) ]; then
                # 结束所有进程
                SELECTED_PIDS=("${PIDS[@]}")
                break
            elif [ "$choice" -ge 1 ] && [ "$choice" -le ${#PIDS[@]} ]; then
                # 结束选择的单个进程
                SELECTED_PID=${PIDS[$((choice-1))]}
                break
            fi
        fi
        echo "无效选择，请重新输入"
    done
fi

# 执行结束进程操作
kill_process() {
    local pid=$1
    echo "正在结束进程 $pid..."

    # 先尝试优雅地结束进程 (SIGTERM)
    echo "发送 SIGTERM 信号给进程 $pid"
    kill $pid 2>/dev/null

    # 等待 3 秒让进程优雅退出
    sleep 3

    # 检查进程是否还在运行
    if kill -0 $pid 2>/dev/null; then
        echo "进程 $pid 仍在运行，强制结束..."
        kill -9 $pid 2>/dev/null
        sleep 1
    fi

    # 验证进程是否已结束
    if kill -0 $pid 2>/dev/null; then
        echo "警告: 进程 $pid 可能仍在运行"
        return 1
    else
        echo "成功结束进程 $pid"
        return 0
    fi
}

# 结束选中的进程
if [ ! -z "$SELECTED_PID" ]; then
    # 单个进程
    kill_process $SELECTED_PID
elif [ ! -z "$SELECTED_PIDS" ]; then
    # 多个进程
    SUCCESS=0
    for pid in "${SELECTED_PIDS[@]}"; do
        if kill_process $pid; then
            ((SUCCESS++))
        fi
    done
    echo "成功结束 $SUCCESS/${#SELECTED_PIDS[@]} 个进程"
fi

# 最终检查端口状态
echo ""
REMAINING_PROCESSES=$(lsof -i :$PORT 2>/dev/null)
if [ -z "$REMAINING_PROCESSES" ]; then
    echo "端口 $PORT 现在没有被任何进程占用"
else
    echo "端口 $PORT 仍有以下进程在使用:"
    echo "$REMAINING_PROCESSES"
fi