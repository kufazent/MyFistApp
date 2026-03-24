package ru.shvetcov.myfirstapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import ru.shvetcov.myfirstapp.activity.EditPostContract
import ru.shvetcov.myfirstapp.adapter.OnPostInteractionListener
import ru.shvetcov.myfirstapp.adapter.PostsAdapter
import ru.shvetcov.myfirstapp.databinding.ActivityMainBinding
import ru.shvetcov.myfirstapp.dto.Post
import ru.shvetcov.myfirstapp.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: PostViewModel by viewModels()
    private var editingPostId: Long = 0L

    private val interactionListener = object : OnPostInteractionListener {
        override fun onLike(post: Post) {
            viewModel.likeById(post.id)
        }
        override fun onShare(post: Post) {
            val shareIntent = Intent().apply { action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, post.content)
                type = "text/plain" }
            val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.share_post_via))
            startActivity(chooserIntent)
            viewModel.shareById(post.id)

        }
        override fun onEdit(post: Post) {
            editingPostId = post.id
            editPostLauncher.launch(post.content)
        }
        override fun onRemove(post: Post) {
            viewModel.removeById(post.id)
            Toast.makeText(this@MainActivity, "Пост удален", Toast.LENGTH_SHORT).show()
        }
        override fun onAvatarClick(post: Post) {
            Toast.makeText(this@MainActivity, "Профиль: ${post.author}", Toast.LENGTH_SHORT).show()
            viewModel.increaseViews(post.id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val adapter = PostsAdapter(interactionListener)
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }
        binding.content.addTextChangedListener { text ->
            viewModel.changeContent(text.toString())
        }
        binding.cancel.setOnClickListener {
            editingPostId = 0L
            binding.content.text.clear()
            binding.cancelGroup.visibility = View.GONE
            hideKeyboard(binding.content)
            viewModel.cancelEdit()
        }
        binding.fab.setOnClickListener {
            editPostLauncher.launch(null)
        }
    }

    private val editPostLauncher = registerForActivityResult(EditPostContract()) { result ->
        if (!result.isNullOrBlank()) {
            if (editingPostId != 0L) {
                viewModel.saveEditedPost(editingPostId, result)
                editingPostId = 0L
            } else {
                viewModel.changeContent(result)
                viewModel.save()
            }
        }
    }
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun showKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
}