<template>
  <v-container>
    <v-row>
        <v-col cols="12" md="8" offset-md="2">
          <v-card>
            <v-card-title>작업일보 작성</v-card-title>
            
            <v-card-text>
              <v-form>
                <v-text-field
                  v-model="form.workDate"
                  label="작업일자"
                  type="date"
                  required
                />
                
                <v-select
                  v-model="form.workTypeCode"
                  :items="workTypeOptions"
                  label="공종"
                  required
                />
                
                <v-textarea
                  v-model="form.workContent"
                  label="작업내용"
                  rows="5"
                  required
                />
                
                <v-file-input
                  v-model="form.photos"
                  label="현장 사진"
                  multiple
                  accept="image/*"
                  prepend-icon="mdi-camera"
                />
              </v-form>
            </v-card-text>
            
            <v-card-actions>
              <v-btn @click="handleCancel">취소</v-btn>
              <v-spacer />
              <v-btn color="primary" @click="handleSave">임시저장</v-btn>
              <v-btn color="primary" @click="handleSubmit">제출</v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { workTypeCodes } from '@/mock-data/reports'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  workDate: new Date().toISOString().split('T')[0],
  workTypeCode: '',
  workContent: '',
  photos: [] as File[],
})

const workTypeOptions = workTypeCodes.map(code => ({
  title: code.name,
  value: code.code,
}))

function handleCancel() {
  router.push('/app/reports')
}

function handleSave() {
  // 프로토타입: 임시저장 시뮬레이션
  alert('임시저장 완료')
}

function handleSubmit() {
  // 프로토타입: 제출 시뮬레이션
  alert('작업일보 제출 완료')
  router.push('/app/reports')
}
</script>

