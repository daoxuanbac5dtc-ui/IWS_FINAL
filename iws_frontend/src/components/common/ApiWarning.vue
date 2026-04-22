<!-- <template>
    <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
        <div class="flex items-center">
            <AlertTriangleIcon class="w-5 h-5 text-yellow-400 mr-2" />
            <span class="text-sm text-yellow-700"> Đang sử dụng dữ liệu mẫu do lỗi kết nối API: {{ error }} </span>
        </div>
    </div>
</template> -->

<script setup>
import { AlertTriangle as AlertTriangleIcon } from 'lucide-vue-next';

defineProps({
    error: String
});
</script>
